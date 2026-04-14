package ru.practicum.ewm.service.service;

import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.dto.EventState;

import java.time.LocalDateTime;

public interface ValidationService {

    void validateEventDateForCreateOrUpdate(LocalDateTime eventDate);

    void validateEventDateForPublish(LocalDateTime eventDate, LocalDateTime publishedOn);

    void validateEventCanBeUpdateByUser(EventState eventState);

    void validateEventCanBePublishByAdmin(EventState eventState);

    void validateEventCanBeRejectByAdmin(EventState eventState);

    void validateParticipationRequest(Long userId, Event event);
}