package ru.practicum.stats.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.service.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<HttpStatus> hit(@Valid @RequestBody EndpointHitDto hitDto) {
        log.info("Received hit request: {}", hitDto);
        statsService.hit(hitDto);
        log.info("Hit processed successfully");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Received stats request - start: {}, end: {}, uris: {}, unique: {}", start, end, uris, unique);
        try {
            List<ViewStatsDto> result = statsService.getStats(start, end, uris, unique);
            log.info("Returning {} stats records", result.size());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error in getStats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/hit/unique")
    public ResponseEntity<Boolean> isUniqueHit(@RequestParam String uri, @RequestParam String ip) {
        log.info("Checking if hit is unique for URI: {}, IP: {}", uri, ip);
        boolean isUnique = statsService.isUniqueHit(uri, ip);
        log.info("Hit is unique: {}", isUnique);
        return ResponseEntity.ok(isUnique);
    }
}
