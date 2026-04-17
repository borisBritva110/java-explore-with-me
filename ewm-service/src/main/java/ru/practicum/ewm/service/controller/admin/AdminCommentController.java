package ru.practicum.ewm.service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.model.CommentStatus;
import ru.practicum.ewm.service.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<CommentDto>> getCommentsForModeration(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "PENDING") CommentStatus status,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получение комментариев для модерации: text={}, status={}, from={}, size={}", text, status, from, size);
        Page<CommentDto> comments = commentService.getCommentsForModeration(text, status, from, size);
        return ResponseEntity.ok(comments);
    }

    @PatchMapping("/{commentId}/publish")
    public ResponseEntity<CommentDto> publishComment(@PathVariable Long commentId) {
        log.info("Публикация комментария с id={}", commentId);
        CommentDto comment = commentService.moderateComment(commentId, CommentStatus.PUBLISHED);
        return ResponseEntity.ok(comment);
    }

    @PatchMapping("/{commentId}/reject")
    public ResponseEntity<CommentDto> rejectComment(@PathVariable Long commentId) {
        log.info("Отклонение комментария с id={}", commentId);
        CommentDto comment = commentService.moderateComment(commentId, CommentStatus.REJECTED);
        return ResponseEntity.ok(comment);
    }
}