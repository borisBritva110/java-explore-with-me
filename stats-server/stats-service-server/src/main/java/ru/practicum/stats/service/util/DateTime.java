package ru.practicum.stats.service.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTime {
    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZONE_OFFSET);
    }
}