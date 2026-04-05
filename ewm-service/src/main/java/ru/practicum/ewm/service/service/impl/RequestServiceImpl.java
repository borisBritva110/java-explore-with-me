package ru.practicum.ewm.service.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.ewm.service.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.service.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.service.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.dto.RequestStatus;
import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.exception.ConflictException;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.mapper.RequestMapper;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.model.ParticipationRequest;
import ru.practicum.ewm.service.model.User;
import ru.practicum.ewm.service.repository.EventRepository;
import ru.practicum.ewm.service.repository.ParticipationRequestRepository;
import ru.practicum.ewm.service.repository.UserRepository;
import ru.practicum.ewm.service.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя с id={}", userId);

        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream()
            .map(RequestMapper::toParticipationRequestDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Добавление запроса на участие пользователя с id={} в событии с id={}", userId, eventId);

        User requester = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        if (eventId == null) {
            throw new ValidationException("EventId не может быть пустым");
        }

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найден"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие в своём событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Запрос на участие уже существует");
        }

        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
            long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников события");
            }
        }

        ParticipationRequest request = ParticipationRequest.builder()
            .event(event)
            .requester(requester)
            .build();

        if ((event.getParticipantLimit() != null && event.getParticipantLimit() == 0) ||
            (event.getRequestModeration() != null && !event.getRequestModeration())) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() != null ?
                event.getConfirmedRequests() + 1 : 1);
            eventRepository.save(event);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса с id={} пользователя с id={}", requestId, userId);

        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
            .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        if (request.getStatus() == RequestStatus.CANCELED) {
            throw new ConflictException("Запрос уже отменен");
        }

        request.setStatus(RequestStatus.CANCELED);

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            if (event.getConfirmedRequests() != null && event.getConfirmedRequests() > 0) {
                event.setConfirmedRequests(event.getConfirmedRequests() - 1);
                eventRepository.save(event);
            }
        }

        ParticipationRequest updatedRequest = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(updatedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.info("Получение запросов на участие в событии с id={} пользователя с id={}", eventId, userId);

        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено у пользователя с id=" + userId);
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
            .map(RequestMapper::toParticipationRequestDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Изменение статуса запросов на участие в событии с id={} пользователя с id={}: {}",
            eventId, userId, updateRequest);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
            .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено у пользователя с id=" + userId));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять статусы запросов для неопубликованного события");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdWithLock(updateRequest.getRequestIds());

        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new NotFoundException("Некоторые запросы не найдены");
        }

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Запрос с id=" + request.getId() + " не принадлежит событию с id=" + eventId);
            }
        }

        long currentConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long participantLimit = event.getParticipantLimit() != null ? event.getParticipantLimit() : 0;

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            for (ParticipationRequest request : requests) {
                if (request.getStatus() != RequestStatus.PENDING) {
                    throw new ConflictException("Можно подтверждать только запросы в статусе PENDING");
                }

                if (participantLimit > 0 && currentConfirmed >= participantLimit) {
                    request.setStatus(RequestStatus.REJECTED);
                    requestRepository.save(request);
                    rejectedRequests.add(RequestMapper.toParticipationRequestDto(request));
                } else {
                    request.setStatus(RequestStatus.CONFIRMED);
                    requestRepository.save(request);
                    confirmedRequests.add(RequestMapper.toParticipationRequestDto(request));
                    currentConfirmed++;

                    event.setConfirmedRequests(currentConfirmed);
                }
            }
        } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            for (ParticipationRequest request : requests) {
                if (request.getStatus() != RequestStatus.PENDING) {
                    throw new ConflictException("Можно отклонять только запросы в статусе PENDING");
                }

                request.setStatus(RequestStatus.REJECTED);
                requestRepository.save(request);
                rejectedRequests.add(RequestMapper.toParticipationRequestDto(request));
            }
        } else {
            throw new ConflictException("Недопустимый статус для изменения: " + updateRequest.getStatus());
        }

        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
            .confirmedRequests(confirmedRequests)
            .rejectedRequests(rejectedRequests)
            .build();
    }
}