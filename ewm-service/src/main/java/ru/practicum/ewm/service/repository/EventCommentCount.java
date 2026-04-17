package ru.practicum.ewm.service.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCommentCount {
    private Long eventId;
    private Long commentCount;
}