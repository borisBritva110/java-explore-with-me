package ru.practicum.ewm.service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.practicum.ewm.service.dto.RequestStatus;
import ru.practicum.ewm.service.model.ParticipationRequest;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.id IN :ids")
    List<ParticipationRequest> findAllByIdWithLock(@Param("ids") List<Long> ids);

    @Modifying
    @Query("UPDATE ParticipationRequest pr SET pr.status = 'REJECTED' " +
        "WHERE pr.event.id = :eventId AND pr.status = 'PENDING'")
    int rejectAllPendingRequestsByEventId(@Param("eventId") Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);
}