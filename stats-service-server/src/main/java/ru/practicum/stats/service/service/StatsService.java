package ru.practicum.stats.service.service;

import lombok.RequiredArgsConstructor;
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
@Transactional
public class StatsService {
    private final StatsRepository statsRepository;

    public void hit(EndpointHitDto hitDto) {
        EndpointHit hit = EndpointHitMapper.toEntity(hitDto);
        statsRepository.save(hit);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            if (nonNull(uris)) {
                List<ViewStatsDto> uniqueWithUrisStats = statsRepository.findUniqueWithUrisStats(uris, start, end);
                return uniqueWithUrisStats;
            } else {
                return statsRepository.findUniqueAndNoUrisStats(start, end);
            }

        } else {
            if (nonNull(uris)) {
                List<ViewStatsDto> noUniqueWithUrisStats = statsRepository.findNoUniqueWithUrisStats(uris, start, end);
                return noUniqueWithUrisStats;
            } else {
                return statsRepository.findNoUniqueAndNoUrisStats(start, end);
            }

        }
    }
}