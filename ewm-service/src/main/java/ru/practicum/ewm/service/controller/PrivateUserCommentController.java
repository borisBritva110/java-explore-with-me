package ru.practicum.ewm.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.dto.UpdateCommentRequest;
import ru.practicum.ewm.service.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateUserCommentController {

    private final CommentService commentService;

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