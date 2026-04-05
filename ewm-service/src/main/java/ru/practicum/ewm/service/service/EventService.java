package ru.practicum.ewm.service.service;

import ru.practicum.ewm.service.dto.EventFullDto;
import ru.practicum.ewm.service.dto.EventShortDto;
import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.dto.NewEventDto;
import ru.practicum.ewm.service.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.service.dto.UpdateEventUserRequest;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EventService {

    List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        boolean onlyAvailable, EventSort sort, int from, int size);

    EventFullDto getEventByIdPublic(Long eventId, String ip);

    List<EventShortDto> getEventsPrivate(Long userId, int from, int size);

    EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByIdPrivate(Long userId, Long eventId);

    EventFullDto updateEventByIdPrivate(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<EventFullDto> getEventsAdmin(List<Long> users, List<EventState> states,
                                      List<Long> categories, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByIdAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    Map<Long, Long> getViewStatsForEvents(List<Event> events);

    Event getEventByIdWithLock(Long eventId);

    List<Event> getAllEventById(Set<Long> ids);

    void updateConfirmedRequests(Long eventId);
}