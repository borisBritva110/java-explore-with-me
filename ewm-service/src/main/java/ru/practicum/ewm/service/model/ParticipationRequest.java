package ru.practicum.ewm.service.model;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import ru.practicum.ewm.service.dto.RequestStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participation_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created")
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now().withNano(0); // Обрезаем наносекунды для соответствия тестам
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }
}
