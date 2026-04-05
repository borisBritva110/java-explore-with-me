package ru.practicum.ewm.service.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.service.dto.EventState;
import ru.practicum.ewm.service.model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> published() {
        return (root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED);
    }

    public static Specification<Event> textContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + text.toLowerCase() + "%";
            Predicate annotationPredicate = cb.like(cb.lower(root.get("annotation")), pattern);
            Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), pattern);
            return cb.or(annotationPredicate, descriptionPredicate);
        };
    }

    public static Specification<Event> categoriesIn(List<Long> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("category").get("id").in(categories);
        };
    }

    public static Specification<Event> paidEquals(Boolean paid) {
        return (root, query, cb) -> {
            if (paid == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("paid"), paid);
        };
    }

    public static Specification<Event> eventDateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return cb.greaterThan(root.get("eventDate"), LocalDateTime.now());
            }

            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> eventDateInFuture() {
        return (root, query, cb) -> cb.greaterThan(root.get("eventDate"), LocalDateTime.now());
    }

    public static Specification<Event> initiatorIdsIn(List<Long> userIds) {
        return (root, query, cb) -> {
            if (userIds == null || userIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("initiator").get("id").in(userIds);
        };
    }

    public static Specification<Event> statesIn(List<EventState> states) {
        return (root, query, cb) -> {
            if (states == null || states.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("state").in(states);
        };
    }

    public static Specification<Event> eventDateBetweenAdmin(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}