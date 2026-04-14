package ru.practicum.ewm.service.dto;

public record EventWithCountConfirmedRequests(
    Long eventId,
    Long countConfirmedRequests
) {}