package ru.practicum.ewm.service.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @Column(name = "title", nullable = false, length = 50, unique = true)
    private String title;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "compilations_events",
        joinColumns = @JoinColumn(name = "compilation_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;
}