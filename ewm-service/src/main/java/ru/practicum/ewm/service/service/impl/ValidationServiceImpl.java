package ru.practicum.ewm.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.dto.RequestStatus;
import ru.practicum.ewm.service.exception.ConflictException;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.repository.ParticipationRequestRepository;
import ru.practicum.ewm.service.service.ValidationService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    private static final int MIN_HOURS_BEFORE_EVENT = 2;
    private static final int MIN_HOURS_BEFORE_PUBLISH = 1;

    private final ParticipationRequestRepository requestRepository;

    @Override
    public void validateEventDateForCreateOrUpdate(LocalDateTime eventDate) {
        LocalDateTime minDate = LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT);
        if (eventDate.isBefore(minDate)) {
            throw new ConflictException(
                "Дата начала события должна быть не ранее чем через " + MIN_HOURS_BEFORE_EVENT + " ч.");
        }
    }

    @Override
    public void validateEventDateForPublish(LocalDateTime eventDate, LocalDateTime publishedOn) {
        LocalDateTime minDate = publishedOn.plusHours(MIN_HOURS_BEFORE_PUBLISH);
        if (eventDate.isBefore(minDate)) {
            throw new ConflictException(
                "Дата начала события должна быть не ранее чем через " + MIN_HOURS_BEFORE_PUBLISH + " ч. после публикации");
        }
    }

    @Override
    public void validateEventCanBeUpdateByUser(EventState eventState) {
        if (eventState == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
    }

    @Override
    public void validateEventCanBePublishByAdmin(EventState eventState) {
        if (eventState != EventState.PENDING) {
            throw new ConflictException("Cannot publish the event because it's not in the right state: " + eventState);
        }
    }

    @Override
    public void validateEventCanBeRejectByAdmin(EventState eventState) {
        if (eventState == EventState.PUBLISHED) {
            throw new ConflictException("Cannot reject the event because it's already published");
        }
    }

    @Override
    public void validateParticipationRequest(Long userId, Event event) {
        if (requestRepository.existsByEventIdAndRequesterId(event.getId(), userId)) {
            throw new ConflictException("Request already exists");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot add request to own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }
        if (event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }
        }
    }
}