package ru.practicum.ewm.service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :categoryId")
    boolean existsEventsByCategoryId(@Param("categoryId") Long categoryId);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :eventId")
    Optional<Event> findByIdWithLock(@Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE Event e SET e.confirmedRequests = " +
        "(SELECT COUNT(pr) FROM ParticipationRequest pr " +
        "WHERE pr.event.id = e.id AND pr.status = 'CONFIRMED') " +
        "WHERE e.id = :eventId")
    void updateConfirmedRequests(@Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id = :eventId")
    void incrementViews(@Param("eventId") Long eventId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);
}