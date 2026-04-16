package ru.practicum.ewm.service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.CommentDto;
import ru.practicum.ewm.service.service.CommentService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<CommentDto>> getCommentsByEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получение комментариев к событию с id={}, from={}, size={}", eventId, from, size);
        Page<CommentDto> comments = commentService.getCommentsByEvent(eventId, from, size);
        return ResponseEntity.ok(comments);
    }
}