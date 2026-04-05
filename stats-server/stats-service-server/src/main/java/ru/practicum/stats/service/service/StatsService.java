package ru.practicum.stats.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.service.mapper.EndpointHitMapper;
import ru.practicum.stats.service.model.EndpointHit;
import ru.practicum.stats.service.repository.StatsRepository;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatsService {
    private final StatsRepository statsRepository;

    public void hit(EndpointHitDto hitDto) {
        log.info("Saving hit for URI: {}, IP: {}, App: {}", hitDto.getUri(), hitDto.getIp(), hitDto.getApp());
        EndpointHit hit = EndpointHitMapper.toEntity(hitDto);
        EndpointHit savedHit = statsRepository.save(hit);
        log.info("Hit saved with ID: {}", savedHit.getId());
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Getting stats - start: {}, end: {}, uris: {}, unique: {}", start, end, uris, unique);
        List<ViewStatsDto> result = switch (unique != null ? (unique ? 1 : 2) : 3) {
            case 1 -> nonNull(uris)
                ? statsRepository.findUniqueWithUrisStats(uris, start, end)
                : statsRepository.findUniqueAndNoUrisStats(start, end);
            case 2 -> nonNull(uris)
                ? statsRepository.findNoUniqueWithUrisStats(uris, start, end)
                : statsRepository.findNoUniqueAndNoUrisStats(start, end);
            default -> throw new IllegalArgumentException("Unique parameter cannot be null");
        };

        log.info("Found {} stats records", result.size());
        result.forEach(stat -> log.info("Stat: {} - {} hits", stat.getUri(), stat.getHits()));

        return result;
    }
}