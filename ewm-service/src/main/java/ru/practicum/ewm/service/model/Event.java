package ru.practicum.ewm.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import ru.practicum.ewm.service.dto.EventState;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 120)
    private String title;
    
    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;
    
    @Column(name = "description", length = 7000)
    private String description;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    @ToString.Exclude
    private User initiator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Embedded
    private Location location;
    
    @Column(name = "paid")
    private Boolean paid;
    
    @Column(name = "participant_limit")
    private Integer participantLimit;
    
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state;
    
    @Column(name = "confirmed_requests")
    private Long confirmedRequests;
    
    @Column(name = "views")
    private Long views;
}