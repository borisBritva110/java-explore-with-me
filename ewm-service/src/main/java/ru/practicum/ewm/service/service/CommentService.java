package ru.practicum.ewm.service.service;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.dto.NewCommentDto;
import ru.practicum.ewm.service.dto.UpdateCommentRequest;
import ru.practicum.ewm.service.model.CommentStatus;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest dto);

    void deleteComment(Long userId, Long commentId);

    CommentDto getCommentById(Long commentId);

    Page<CommentDto> getCommentsByEvent(Long eventId, Integer from, Integer size);

    Page<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size);

    CommentDto moderateComment(Long commentId, CommentStatus status);

    Page<CommentDto> getCommentsForModeration(String text, CommentStatus status, Integer from, Integer size);

    List<CommentDto> getCommentsByEventIds(List<Long> eventIds);
}