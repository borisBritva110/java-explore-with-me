package ru.practicum.ewm.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.dto.NewCommentDto;
import ru.practicum.ewm.service.dto.UpdateCommentRequest;
import ru.practicum.ewm.service.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto dto) {
        
        log.info("Создание комментария пользователем с id={} к событию с id={}: {}", userId, eventId, dto);
        CommentDto comment = commentService.createComment(userId, eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest dto) {
        
        log.info("Обновление комментария с id={} пользователем с id={}: {}", commentId, userId, dto);
        CommentDto comment = commentService.updateComment(userId, commentId, dto);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
        
        log.info("Удаление комментария с id={} пользователем с id={}", commentId, userId);
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CommentDto>> getCommentsByAuthor(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("Получение комментариев пользователя с id={}, from={}, size={}", userId, from, size);
        Page<CommentDto> comments = commentService.getCommentsByAuthor(userId, from, size);
        return ResponseEntity.ok(comments);
    }
}