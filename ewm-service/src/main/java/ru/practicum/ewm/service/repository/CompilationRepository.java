package ru.practicum.ewm.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.service.model.Compilation;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @EntityGraph(attributePaths = {"events"})
    Optional<Compilation> findById(Long id);

    @EntityGraph(attributePaths = {"events"})
    @Query("SELECT c FROM Compilation c " +
        "WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    Page<Compilation> findAllWithFilters(@Param("pinned") Boolean pinned, Pageable pageable);

    @Query("SELECT c FROM Compilation c LEFT JOIN FETCH c.events WHERE c.id = :compilationId")
    Optional<Compilation> findByIdWithEvents(@Param("compilationId") Long compilationId);

}