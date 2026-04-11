package ru.practicum.ewm.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.service.exception.BadRequestException;
import ru.practicum.ewm.service.exception.ConflictException;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.dto.RequestStatus;
import ru.practicum.ewm.service.repository.*;
import ru.practicum.ewm.service.service.ValidationService;
import ru.practicum.ewm.service.model.NotFound;

import java.time.LocalDateTime;

import jakarta.validation.ValidationException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl implements ValidationService {
    private static final int MIN_HOURS_BEFORE_EVENT = 2;
    private static final int MIN_HOURS_BEFORE_PUBLISH = 1;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final CompilationRepository compilationRepository;

    @Override
    public void validateEventDateForCreateOrUpdate(LocalDateTime eventDate) {
        LocalDateTime now = LocalDateTime.now();
        log.info("Проверяем, что дата начала события {} не ранее чем через 2 часа от сейчас {}",
            eventDate, now);
        LocalDateTime minEventDate = now.plusHours(MIN_HOURS_BEFORE_EVENT);

        if (eventDate.isBefore(minEventDate)) {
            throw new ConflictException(String.format(
                "Field: eventDate. Error: дата начала события должна быть не ранее чем через " +
                    "%dч. от текущего времени %s. Value: %s",
                MIN_HOURS_BEFORE_EVENT, now, eventDate));
        }
    }

    @Override
    public void validateEventDateForPublish(LocalDateTime eventDate, LocalDateTime publishedOn) {
        log.info("Проверяем, что дата начала изменяемого события {} не ранее чем через час от даты публикации {}",
            eventDate, publishedOn);

        LocalDateTime minEventDate = publishedOn.plusHours(MIN_HOURS_BEFORE_PUBLISH);

        if (eventDate.isBefore(minEventDate)) {
            throw new ConflictException(String.format(
                "Field: eventDate. Error: дата начала изменяемого события должна быть не ранее чем через " +
                    "%dч. от даты публикации %s. Value: %s",
                MIN_HOURS_BEFORE_PUBLISH, publishedOn, eventDate));
        }
    }

    @Override
    public void validateEventCanBeUpdateByUser(EventState eventState) {
        log.info("Проверяем, что событие ещё не опубликовано(статус:{}), т.е. можно редактировать", eventState);
        if (eventState == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
    }

    @Override
    public void validateEventCanBePublishByAdmin(EventState eventState) {
        log.info("(админ)Проверяем можно ли опубликовать событие(статус:{})", eventState);
        if (eventState != EventState.PENDING) {
            throw new ConflictException(
                String.format("Cannot publish the event because it's not in the right state: %s", eventState));
        }
    }

    @Override
    public void validateEventCanBeRejectByAdmin(EventState eventState) {
        log.info("(админ)Проверяем можно ли отклонить публикацию события(статус:{})", eventState);
        if (eventState == EventState.PUBLISHED) {
            throw new ConflictException("Cannot reject the event because it's already published");
        }
    }

    @Override
    public void validateEventExistsAndInitiator(Long eventId, Long userId) {
        if (eventRepository.existsByIdAndInitiatorId(eventId, userId) == false) {
            throw new NotFoundException(
                String.format(NotFound.EVENT + " or user is not the initiator that event", eventId));
        }
    }

    @Override
    public void checkUserExists(Long userId) {
        log.info("Проверяем существует ли пользователь id {}", userId);
        if (userRepository.existsById(userId) == false) {
            throw new NotFoundException(
                String.format(NotFound.USER, userId));
        }
    }

    @Override
    public void checkUserEmailUse(String email) {
        log.info("Проверяем занят ли email {}.", email);
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with Email " + email + "already exists");
        }
        log.info("email {} свободен.", email);
    }

    @Override
    public void validateCategoryDeletion(Long catId) {
        log.info("Проверяем условия для удаления категории {}", catId);
        checkCategoryExists(catId);
        if (eventRepository.existsEventsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }
    }

    @Override
    public void checkCategoryExists(Long catId) {
        log.info("Проверяем наличие категории {}", catId);
        if (categoryRepository.existsById(catId) == false) {
            throw new NotFoundException(
                String.format(NotFound.CATEGORY, catId));
        }
    }

    @Override
    public void checkCategoryNameUse(String name, Long catId) {
        log.info("Проверяем занято ли имя категории {}.", name);

        if (name.isEmpty()) {
            throw new ValidationException("Длина имени категории не может быть пустым");
        }
        if (name.length() > 50) {
            throw new ValidationException("Длина имени категории должна быть меньше 50 символов");
        }

        boolean exists = (catId == null)
            ? categoryRepository.existsByName(name)
            : categoryRepository.existsByNameAndIdNot(name, catId);

        if (exists) {
            throw new ConflictException("Category with name " + name + " already exists");
        }

        log.info("имя {} свободно.", name);
    }

    @Override
    public void validateParticipationRequest(Long userId, Event event) {
        log.info("Проверяем условия для подачи заявки на участие пользователя id {} в событии id {}", userId, event);

        checkUserExists(userId);

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

    @Override
    public void validateRequestCanBeReject(RequestStatus requestStatus) {
        if (requestStatus == RequestStatus.REJECTED) {
            throw new ConflictException("Cannot cancel rejected request");
        }

        if (requestStatus == RequestStatus.CANCELED) {
            throw new ConflictException("Request is already canceled");
        }
    }

    @Override
    public void checkCompilationExists(Long compId) {
        log.info("Проверяем наличие подборки id {}", compId);
        if (compilationRepository.existsById(compId) == false) {
            throw new NotFoundException(
                String.format(NotFound.COMPILATION, compId));
        }
    }

    @Override
    public void checkCompilationTitleUse(String title) {
        log.info("Проверяем занято ли название подборки {}.", title);
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Длина имени не может быть пустой");
        }
        if (compilationRepository.existsByTitle(title)) {
            throw new ValidationException("Compilation with title " + title + " already exists");
        }
        if (title.length() > 50) {
            throw new ValidationException("Длина заголовка должна быть меньше 50 символов");
        }
        log.info("название {} свободно.", title);
    }

    @Override
    public void validateDateForSearch(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        log.info("Проверяем, диапазон дат для поиска: start {} - end {}",
            rangeStart, rangeEnd);

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException(String.format(
                "Error: rangeEnd %s не может быть меньше rangeStart %s",
                rangeEnd, rangeStart));
        }
    }
}