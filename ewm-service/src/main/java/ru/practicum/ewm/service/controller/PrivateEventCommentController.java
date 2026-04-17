package ru.practicum.ewm.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.dto.NewCommentDto;
import ru.practicum.ewm.service.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/comments")
public class PrivateEventCommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
        @PathVariable Long userId,
        @PathVariable Long eventId,
        @Valid @RequestBody NewCommentDto dto) {

        log.info("Создание комментария пользователем с id={} к событию с id={}: {}", userId, eventId, dto);
        CommentDto comment = commentService.createComment(userId, eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}