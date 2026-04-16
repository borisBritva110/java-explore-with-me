package ru.practicum.ewm.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.service.model.Comment;
import ru.practicum.ewm.service.model.CommentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEventIdAndStatusOrderByCreatedOnDesc(Long eventId, CommentStatus status, Pageable pageable);

    Page<Comment> findByAuthorIdOrderByCreatedOnDesc(Long authorId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE (:text IS NULL OR LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
           "c.status = :status ORDER BY c.createdOn DESC")
    Page<Comment> findByTextAndStatus(@Param("text") String text, @Param("status") CommentStatus status, Pageable pageable);

    List<Comment> findByEventIdInAndStatusEquals(List<Long> eventIds, CommentStatus status);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);
}