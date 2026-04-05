package ru.practicum.ewm.service.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping("/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("Получение запросов пользователя с id={}", userId);

        List<ParticipationRequestDto> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests")
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(@PathVariable long userId, @PositiveOrZero Long eventId) {
        log.info("Добавление запроса на участие пользователя с id={} в событии с id={}", userId, eventId);
        ParticipationRequestDto requestDto = requestService.addParticipationRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestDto);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
        @PathVariable Long userId,
        @PathVariable Long requestId) {

        log.info("Отмена запроса с id={} пользователя с id={}", requestId, userId);

        ParticipationRequestDto requestDto = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(requestDto);
    }
}