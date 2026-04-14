package ru.practicum.stats.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotFound {
    public static final String USER = "User with id=%d was not found";
    public static final String CATEGORY = "Category with id=%d was not found";
    public static final String EVENT = "Event with id=%d was not found";
    public static final String COMPILATION = "Compilation with id=%d was not found";
    public static final String REQUEST = "Request with id=%d was not found";
}