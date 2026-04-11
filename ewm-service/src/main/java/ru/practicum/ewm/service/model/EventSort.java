package ru.practicum.ewm.service.model;

import java.util.Optional;

public enum EventSort {
    EVENT_DATE,
    VIEWS;

    public static Optional<EventSort> from(String sort) {
        for (EventSort eventSort : values()) {
            if (eventSort.name().equalsIgnoreCase(sort)) {
                return Optional.of(eventSort);
            }
        }
        return Optional.empty();
    }
}