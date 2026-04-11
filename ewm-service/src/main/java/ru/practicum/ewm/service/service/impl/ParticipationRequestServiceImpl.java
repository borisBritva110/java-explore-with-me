package ru.practicum.ewm.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.service.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.service.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.exception.ConflictException;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.mapper.RequestMapper;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.model.NotFound;
import ru.practicum.ewm.service.model.ParticipationRequest;
import ru.practicum.ewm.service.dto.RequestStatus;
import ru.practicum.ewm.service.repository.ParticipationRequestRepository;
import ru.practicum.ewm.service.repository.UserRepository;
import ru.practicum.ewm.service.service.EventService;
import ru.practicum.ewm.service.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final ValidationServiceImpl validationService;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получаем запросы пользователя id {}", userId);

        validationService.checkUserExists(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
            .map(RequestMapper::toParticipationRequestDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Добавляем заявку пользователя id {} на событие id {}", userId, eventId);

        Event event = eventService.getEventByIdWithLock(eventId);
        validationService.validateParticipationRequest(userId, event);

        RequestStatus initialStatus;
        if ((event.getParticipantLimit() != null && event.getParticipantLimit() == 0) ||
            (event.getRequestModeration() != null && !event.getRequestModeration())) {
            initialStatus = RequestStatus.CONFIRMED;
        } else {
            initialStatus = RequestStatus.PENDING;
        }

        ParticipationRequest request = ParticipationRequest.builder()
            .created(LocalDateTime.now())
            .event(event)
            .requester(userRepository.getReferenceById(userId))
            .status(initialStatus)
            .build();

        request = requestRepository.save(request);

        if (initialStatus == RequestStatus.CONFIRMED) {
            eventService.updateConfirmedRequests(eventId);
        }

        log.info("Добавлена заявка: {}", request);
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отменяем заявку id {} пользователя id {}", requestId, userId);

        validationService.checkUserExists(userId);

        ParticipationRequest request = getRequestOrThrow(
            requestRepository.findByIdAndRequesterId(requestId, userId),requestId);

        RequestStatus requestStatus = request.getStatus();
        validationService.validateRequestCanBeReject(requestStatus);
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);

        if (requestStatus == RequestStatus.CONFIRMED) {
            eventService.updateConfirmedRequests(request.getEvent().getId());
        }

        log.info("Заявка id {} отменена", requestId);
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получаем заявки для события id {} пользователя id {}", eventId, userId);

        validationService.checkUserExists(userId);
        validationService.validateEventExistsAndInitiator(eventId, userId);

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()
            .map(RequestMapper::toParticipationRequestDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(
        Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest) {

        validationService.checkUserExists(userId);
        Event event = eventService.getEventByIdWithLock(eventId);

        if (event.getInitiator().getId().equals(userId) == false) {
            throw new NotFoundException("User is not initiator");
        }

        if ((event.getParticipantLimit() != null && event.getParticipantLimit() == 0) ||
            (event.getRequestModeration() != null && !event.getRequestModeration())) {
            throw new ConflictException("Request confirmation is not required for this event");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdWithLock(
            statusUpdateRequest.getRequestIds());

        List<Long> wrongRequestIds = requests.stream()
            .filter(r -> r.getEvent().getId().equals(eventId) == false)
            .map(ParticipationRequest::getId)
            .collect(Collectors.toList());

        if (wrongRequestIds.isEmpty() == false) {
            throw new ConflictException("Requests - " + wrongRequestIds + " - not belong to this event");
        }

        List<Long> notPendingIds = requests.stream()
            .filter(r -> r.getStatus() != RequestStatus.PENDING)
            .map(ParticipationRequest::getId)
            .collect(Collectors.toList());

        if (notPendingIds.isEmpty() == false) {
            throw new ConflictException("Requests - " + notPendingIds + " - have not status PENDING");
        }

        long currentConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long availableSlots = event.getParticipantLimit() - currentConfirmed;

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        if (statusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
            if (availableSlots <= 0) {
                throw new ConflictException("The participant limit has been reached");
            }

            for (ParticipationRequest request : requests) {
                if (availableSlots > 0) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.toParticipationRequestDto(request));
                    availableSlots--;
                }
            }

            if (availableSlots == 0) {
                int rejectedCount = requestRepository.rejectAllPendingRequestsByEventId(eventId);
                log.info("Отклонили {} pending requests для события {}", rejectedCount, eventId);

                List<ParticipationRequest> allRejected = requestRepository.findAllByEventIdAndStatus(
                    eventId, RequestStatus.REJECTED);
                rejected = allRejected.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .collect(Collectors.toList());
            }
        } else {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(request));
            }
        }

        requestRepository.saveAll(requests);
        eventService.updateConfirmedRequests(eventId);

        return EventRequestStatusUpdateResult.builder()
            .confirmedRequests(confirmed)
            .rejectedRequests(rejected)
            .build();
    }

    private ParticipationRequest getRequestOrThrow(Optional<ParticipationRequest> requestOpt, Long requestId) {
        return requestOpt
            .orElseThrow(() -> new NotFoundException(
                String.format(NotFound.REQUEST, requestId)));
    }
}