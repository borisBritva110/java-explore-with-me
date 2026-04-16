package ru.practicum.ewm.service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.dto.NewCommentDto;
import ru.practicum.ewm.service.dto.UpdateCommentRequest;
import ru.practicum.ewm.service.exception.ConflictException;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.mapper.CommentMapper;
import ru.practicum.ewm.service.model.Comment;
import ru.practicum.ewm.service.model.CommentStatus;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.model.User;
import ru.practicum.ewm.service.repository.CommentRepository;
import ru.practicum.ewm.service.repository.EventRepository;
import ru.practicum.ewm.service.repository.UserRepository;
import ru.practicum.ewm.service.service.CommentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        User author = getUserById(userId);
        Event event = getEventById(eventId);

        Comment comment = commentMapper.toEntity(dto, event, author);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest dto) {
        Comment comment = getCommentByIdAndAuthor(commentId, userId);

        if (comment.getStatus() == CommentStatus.PUBLISHED) {
            throw new ConflictException("Нельзя редактировать опубликованный комментарий");
        }

        comment.setText(dto.getText());
        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentByIdAndAuthor(commentId, userId);
        commentRepository.delete(comment);
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        return commentMapper.toDto(comment);
    }

    @Override
    public Page<CommentDto> getCommentsByEvent(Long eventId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());
        Page<Comment> comments = commentRepository.findByEventIdAndStatusOrderByCreatedOnDesc(
                eventId, CommentStatus.PUBLISHED, pageable);

        return comments.map(commentMapper::toDto);
    }

    @Override
    public Page<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());
        Page<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedOnDesc(userId, pageable);

        return comments.map(commentMapper::toDto);
    }

    @Override
    @Transactional
    public CommentDto moderateComment(Long commentId, CommentStatus status) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));

        comment.setStatus(status);
        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toDto(updatedComment);
    }

    @Override
    public Page<CommentDto> getCommentsForModeration(String text, CommentStatus status, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> comments = commentRepository.findByTextAndStatus(text, status, pageable);

        return comments.map(commentMapper::toDto);
    }

    @Override
    public List<CommentDto> getCommentsByEventIds(List<Long> eventIds) {
        List<Comment> comments = commentRepository.findByEventIdInAndStatusEquals(eventIds, CommentStatus.PUBLISHED);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
    }

    private Comment getCommentByIdAndAuthor(Long commentId, Long authorId) {
        return commentRepository.findByIdAndAuthorId(commentId, authorId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден или вы не являетесь его автором"));
    }
}