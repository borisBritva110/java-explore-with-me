package ru.practicum.ewm.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.dto.*;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.mapper.EventMapper;
import ru.practicum.ewm.service.model.Category;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.model.EventSort;
import ru.practicum.ewm.service.model.NotFound;
import ru.practicum.ewm.service.model.User;
import ru.practicum.ewm.service.repository.EventRepository;
import ru.practicum.ewm.service.service.CategoryService;
import ru.practicum.ewm.service.service.EventService;
import ru.practicum.ewm.service.service.UserService;
import ru.practicum.ewm.service.specification.EventSpecification;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.validation.ValidationException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsClient statsClient;
    private final ValidationServiceImpl validationService;

    @Override
    public List<EventShortDto> getEventsPrivate(Long userId, int from, int size) {
        log.info("Получаем события пользователя {}, from {}, size {}", userId, from, size);

        validationService.checkUserExists(userId);

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();

        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Long> viewsMap = getViewStatsForEvents(events);
        log.info("(пользователь) Получили для объединения события: {}\n и карту просмотров: {}", events, viewsMap);
        return events.stream()
            .map(event ->
                EventMapper.toEventShortDto(
                    event, viewsMap
                        .getOrDefault(event.getId(), EventMapper.NO_VIEWS)))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto) {
        log.info("Для пользователя id {}, добавляем новое событие: {}", userId, newEventDto);
        if (newEventDto.getDescription() == null || newEventDto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (newEventDto.getAnnotation() == null || newEventDto.getAnnotation().trim().isEmpty()) {
            throw new ValidationException("Аннотация не может быть пустой");
        }
        validationService.validateEventDateForCreateOrUpdate(newEventDto.getEventDate());
        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        Event newEvent = EventMapper.toEvent(newEventDto, user, category);
        newEvent = eventRepository.save(newEvent);
        log.info("Добавлено новое событие: {}", newEvent);
        return EventMapper.toEventFullDto(newEvent, EventMapper.NO_VIEWS);
    }

    @Override
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        log.info("Получаем событие id {} пользователя id {}", eventId, userId);
        validationService.checkUserExists(userId);
        Event event = getEventOrThrow(
            eventRepository.findByIdAndInitiatorId(eventId, userId), eventId);
        Long views = getViewStatsForEvents(List.of(event))
            .getOrDefault(eventId, EventMapper.NO_VIEWS);
        log.info("Получили для объединения событие: {}\n и карту просмотров: {}", event, views);
        return EventMapper.toEventFullDto(event, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByIdPrivate(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Пользователь id {} обновляет событие id {}: {}", eventId, userId, updateRequest);
        if (updateRequest.hasParticipantLimit() && updateRequest.getParticipantLimit() < 0) {
            throw new ValidationException("Число участников не может быть меньше 0");
        }

        validationService.checkUserExists(userId);

        Event event = getEventOrThrow(
            eventRepository.findByIdAndInitiatorId(eventId, userId), eventId);

        validationService.validateEventCanBeUpdateByUser(event.getState());

        EventMapper.updateEventFieldsByUser(event, updateRequest);

        if (updateRequest.hasCategory()) {
            event.setCategory(
                categoryService.getCategoryById(updateRequest.getCategory()));
        }

        if (updateRequest.hasEventDate()) {
            validationService.validateEventDateForCreateOrUpdate(updateRequest.getEventDate());
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.hasStateAction()) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        Long views = getViewStatsForEvents(List.of(updatedEvent))
            .getOrDefault(eventId, EventMapper.NO_VIEWS);

        log.info("Пользователем обновлено событие: {}", updatedEvent);
        return EventMapper.toEventFullDto(updatedEvent, views);
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               boolean onlyAvailable, EventSort sort, int from, int size) {
        log.info("Получаем события, фильтры: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                "onlyAvailable={}, sort={}, from={}, size={}",
            text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        validationService.validateDateForSearch(rangeStart, rangeEnd);

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        Specification<Event> spec = Specification.where(EventSpecification.published())
            .and(EventSpecification.textContains(text))
            .and(EventSpecification.categoriesIn(categories))
            .and(EventSpecification.paidEquals(paid))
            .and(EventSpecification.eventDateBetween(rangeStart, rangeEnd));

        int pageNumber = from / size;
        Pageable pageable;
        if (sort == EventSort.EVENT_DATE) {
            pageable = PageRequest.of(pageNumber, size, Sort.by("eventDate").ascending());
        } else {
            pageable = PageRequest.of(pageNumber, size);
        }

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        if (onlyAvailable) {
            events = events.stream()
                .filter(event -> event.getParticipantLimit() == 0 ||
                    event.getConfirmedRequests() < event.getParticipantLimit())
                .collect(Collectors.toList());
        }

        Map<Long, Long> viewsMap = getViewStatsForEvents(events);

        List<EventShortDto> result = events.stream()
            .map(event -> EventMapper.toEventShortDto(
                event, viewsMap
                    .getOrDefault(event.getId(), EventMapper.NO_VIEWS)))
            .collect(Collectors.toList());

        if (sort == EventSort.VIEWS) {
            result.sort((e1, e2) -> e2.getViews().compareTo(e1.getViews()));
        }
        log.debug("Получили события: {}", result);
        return result;
    }

    @Override
    @Transactional
    public EventFullDto getEventByIdPublic(Long eventId, String ip) {
        log.info("Получаем событие id {}", eventId);
        Event event = getEventOrThrow(eventRepository.findByIdAndState(eventId, EventState.PUBLISHED), eventId);

        event = getEventOrThrow(
            eventRepository.findByIdAndState(eventId, EventState.PUBLISHED), eventId);

        Long views = event.getViews() != null ? event.getViews() : EventMapper.NO_VIEWS;

        log.info("Получили для объединения событие: {}\n и просмотры: {}", event, views);
        return EventMapper.toEventFullDto(event, views);
    }

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<EventState> states,
                                             List<Long> categories, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, int from, int size) {
        log.info("Админ получает события, фильтры: users={}, states={}, categories={}, rangeStart={}," +
                " rangeEnd={}, from={}, size={}",
            users, states, categories, rangeStart, rangeEnd, from, size);

        Specification<Event> spec = Specification
            .where(EventSpecification.initiatorIdsIn(users))
            .and(EventSpecification.statesIn(states))
            .and(EventSpecification.categoriesIn(categories))
            .and(EventSpecification.eventDateBetweenAdmin(rangeStart, rangeEnd));

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        Map<Long, Long> viewsMap = getViewStatsForEvents(events);
        log.info("(админ) Получили для объединения события: {}\n и карту просмотров: {}", events, viewsMap);
        return events.stream()
            .map(event -> EventMapper.toEventFullDto(
                event, viewsMap
                    .getOrDefault(event.getId(),EventMapper.NO_VIEWS)))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByIdAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Админ обновляет событие id {}: {}", eventId, updateRequest);

        Event event = getEventOrThrow(
            eventRepository.findById(eventId), eventId);

        EventMapper.updateEventFieldsByAdmin(event, updateRequest);

        if (updateRequest.getCategory() != null) {
            event.setCategory(
                categoryService.getCategoryById(updateRequest.getCategory()));
        }

        if (updateRequest.getEventDate() != null) {
            if (event.getPublishedOn() != null) {
                validationService.validateEventDateForPublish(
                    updateRequest.getEventDate(), event.getPublishedOn());
            }
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    validationService.validateEventCanBePublishByAdmin(event.getState());
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    validationService.validateEventCanBeRejectByAdmin(event.getState());
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);

        Long views = getViewStatsForEvents(List.of(updatedEvent))
            .getOrDefault(eventId, EventMapper.NO_VIEWS);

        log.info("Админом обновлено событие: {}", updatedEvent);
        return EventMapper.toEventFullDto(updatedEvent, views);
    }

    @Override
    @Transactional
    public void updateConfirmedRequests(Long eventId) {
        log.info("Обновляем у события id {} количество подтвержденных заявок", eventId);
        eventRepository.updateConfirmedRequests(eventId);
    }

    @Override
    public Map<Long, Long> getViewStatsForEvents(List<Event> events) {
        log.info("Получаем просмотры для событий: {}", events);
        if (events == null || events.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> publishedEventIds = new ArrayList<>();
        LocalDateTime earliestPublishedDate = null;
        LocalDateTime publishedDate;

        for (Event event : events) {
            publishedDate = event.getPublishedOn();
            if (event.getState() == EventState.PUBLISHED && publishedDate != null) {
                publishedEventIds.add(event.getId());
                if (earliestPublishedDate == null || publishedDate.isBefore(earliestPublishedDate)) {
                    earliestPublishedDate = publishedDate;
                }
            }
        }

        if (publishedEventIds.isEmpty()) {
            return new HashMap<>();
        }

        List<String> uris = publishedEventIds.stream()
            .map(id -> "/events/" + id)
            .collect(Collectors.toList());

        LocalDateTime start = earliestPublishedDate;
        LocalDateTime end = LocalDateTime.now();

        log.debug("Запрашиваем статистику для опубликованных событий ids {} за период с {} до {}", publishedEventIds, start, end);
        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        Map<Long, Long> viewsMap = new HashMap<>();

        String uri;
        for (ViewStatsDto stat : stats) {
            uri = stat.getUri();
            try {
                Long eventId = Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
                viewsMap.put(eventId, stat.getHits());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to parse eventId from uri: " + uri);
            }
        }

        for (Long eventId : publishedEventIds) {
            viewsMap.putIfAbsent(eventId, EventMapper.NO_VIEWS);
        }
        log.info("Получили карту просмотров: {}", viewsMap);
        return viewsMap;
    }

    @Override
    public Event getEventByIdWithLock(Long eventId) {
        log.info("Получаем событие id {}, блокируя изменение записи", eventId);
        return getEventOrThrow(
            eventRepository.findByIdWithLock(eventId), eventId);
    }

    @Override
    public List<Event> getAllEventById(Set<Long> ids) {
        log.info("Получаем события ids {}", ids);
        return eventRepository.findAllById(ids);
    }

    private Event getEventOrThrow(Optional<Event> eventOpt, Long eventId) {
        return eventOpt
            .orElseThrow(() -> new NotFoundException(
                String.format(NotFound.EVENT, eventId)));
    }
}