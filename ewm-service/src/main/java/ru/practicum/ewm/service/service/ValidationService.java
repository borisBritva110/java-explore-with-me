package ru.practicum.ewm.service.service;

import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.dto.RequestStatus;

import java.time.LocalDateTime;

public interface ValidationService {

    void validateEventDateForCreateOrUpdate(LocalDateTime eventDate);

    void validateEventDateForPublish(LocalDateTime eventDate, LocalDateTime publishedOn);

    void validateEventCanBeUpdateByUser(EventState eventState);

    void validateEventCanBePublishByAdmin(EventState eventState);

    void validateEventCanBeRejectByAdmin(EventState eventState);

    void validateEventExistsAndInitiator(Long eventId, Long userId);

    void checkUserExists(Long userId);

    void checkUserEmailUse(String email);

    void validateCategoryDeletion(Long catId);

    void checkCategoryExists(Long catId);

    void checkCategoryNameUse(String name, Long catId);

    void validateParticipationRequest(Long userId, Event event);

    void validateRequestCanBeReject(RequestStatus requestStatus);

    void checkCompilationExists(Long compId);

    void checkCompilationTitleUse(String title);

    void validateDateForSearch(LocalDateTime rangeStart, LocalDateTime rangeEnd);
}