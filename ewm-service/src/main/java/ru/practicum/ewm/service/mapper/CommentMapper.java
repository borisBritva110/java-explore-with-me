package ru.practicum.ewm.service.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.dto.NewCommentDto;
import ru.practicum.ewm.service.model.Comment;
import ru.practicum.ewm.service.model.CommentStatus;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.model.User;

@Component
public class CommentMapper {
    
    public Comment toEntity(NewCommentDto dto, Event event, User author) {
        return Comment.builder()
                .text(dto.getText())
                .event(event)
                .author(author)
                .status(CommentStatus.PENDING)
                .build();
    }
    
    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEvent().getId())
                .author(UserMapper.toShortDto(comment.getAuthor()))
                .createdOn(comment.getCreatedOn())
                .updatedOn(comment.getUpdatedOn())
                .status(comment.getStatus())
                .build();
    }
}