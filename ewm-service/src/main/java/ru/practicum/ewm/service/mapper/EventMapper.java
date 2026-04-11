package ru.practicum.ewm.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.dto.*;
import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.model.Category;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {
    private static final Integer PARTICIPANT_LIMIT_DEFAULT_VALUE = 0;
    private static final Long CONFIRMED_REQUESTS_DEFAULT_VALUE = 0L;
    private static final Boolean REQUEST_MODERATION_DEFAULT_VALUE = true;
    private static final Boolean PAID_DEFAULT_VALUE = false;
    public static final Long NO_VIEWS = 0L;

    public static Event toEvent(NewEventDto newEventDto, User initiator, Category category) {
        return Event.builder()
            .annotation(newEventDto.getAnnotation())
            .category(category)
            .confirmedRequests(CONFIRMED_REQUESTS_DEFAULT_VALUE)
            .createdOn(LocalDateTime.now())
            .description(newEventDto.getDescription())
            .eventDate(newEventDto.getEventDate())
            .initiator(initiator)
            .location(LocationMapper.toLocation(newEventDto.getLocation()))
            .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : PAID_DEFAULT_VALUE)
            .participantLimit(newEventDto
                .getParticipantLimit() != null ? newEventDto.getParticipantLimit() : PARTICIPANT_LIMIT_DEFAULT_VALUE)
            .requestModeration(newEventDto
                .getRequestModeration() != null ? newEventDto.getRequestModeration() : REQUEST_MODERATION_DEFAULT_VALUE)
            .state(EventState.PENDING)
            .title(newEventDto.getTitle())
            .build();
    }

    public static EventShortDto toEventShortDto(Event event, Long views) {
        return EventShortDto.builder()
            .id(event.getId())
            .annotation(event.getAnnotation())
            .category(CategoryMapper.toCategoryDto(event.getCategory()))
            .confirmedRequests(event.getConfirmedRequests())
            .eventDate(event.getEventDate())
            .initiator(UserMapper.toShortDto(event.getInitiator()))
            .paid(event.getPaid())
            .title(event.getTitle())
            .views(views)
            .build();
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder()
            .id(event.getId())
            .annotation(event.getAnnotation())
            .category(CategoryMapper.toCategoryDto(event.getCategory()))
            .confirmedRequests(confirmedRequests)
            .createdOn(event.getCreatedOn())
            .description(event.getDescription())
            .eventDate(event.getEventDate())
            .initiator(UserMapper.toShortDto(event.getInitiator()))
            .location(LocationMapper.toLocationDto(event.getLocation()))
            .paid(event.getPaid())
            .participantLimit(event.getParticipantLimit())
            .publishedOn(event.getPublishedOn())
            .requestModeration(event.getRequestModeration())
            .state(event.getState().name())
            .title(event.getTitle())
            .views(views)
            .build();
    }

    public static void updateEventFieldsByUser(Event event, UpdateEventUserRequest updateRequest) {

        if (updateRequest.hasAnnotation()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.hasDescription()) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.hasLocation()) {
            event.setLocation(LocationMapper.toLocation(updateRequest.getLocation()));
        }

        if (updateRequest.hasPaid()) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.hasParticipantLimit()) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.hasRequestModeration()) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.hasTitle()) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    public static void updateEventFieldsByAdmin(Event event, UpdateEventAdminRequest updateRequest) {

        if (updateRequest.hasAnnotation()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.hasDescription()) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.hasLocation()) {
            event.setLocation(LocationMapper.toLocation(updateRequest.getLocation()));
        }

        if (updateRequest.hasPaid()) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.hasParticipantLimit()) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.hasRequestModeration()) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.hasTitle()) {
            event.setTitle(updateRequest.getTitle());
        }
    }
}