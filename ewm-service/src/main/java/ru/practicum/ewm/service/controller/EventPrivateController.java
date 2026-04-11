package ru.practicum.ewm.service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.service.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.service.dto.NewEventDto;
import ru.practicum.ewm.service.dto.UpdateEventUserRequest;
import ru.practicum.ewm.service.dto.EventFullDto;
import ru.practicum.ewm.service.dto.EventShortDto;
import ru.practicum.ewm.service.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.service.EventService;
import ru.practicum.ewm.service.service.ParticipationRequestService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@PathVariable long userId,
                                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                         @RequestParam(defaultValue = "10") @Positive int size) {
        return ResponseEntity.ok(
            eventService.getEventsPrivate(userId, from, size));
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addEvent(@PathVariable long userId,
                                                 @Valid @RequestBody NewEventDto newEventDto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(eventService.addEventPrivate(userId, newEventDto));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable long userId, @PathVariable long eventId) {
        return ResponseEntity.ok(
            eventService.getEventByIdPrivate(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable long userId, @PathVariable long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return ResponseEntity.ok(
            eventService.updateEventByIdPrivate(userId, eventId, updateRequest));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventParticipants(@PathVariable long userId,
                                                                              @PathVariable long eventId) {
        return ResponseEntity.ok(
            participationRequestService.getEventRequests(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> changeRequestStatus(
        @PathVariable long userId,
        @PathVariable long eventId,
        @Valid @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        return ResponseEntity.ok(
            participationRequestService.changeRequestStatus(userId, eventId, statusUpdateRequest));
    }
}