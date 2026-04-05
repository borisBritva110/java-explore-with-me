package ru.practicum.stats.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.practicum.stats.service.model.EndpointHit;
import ru.practicum.stats.dto.ViewStatsDto;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
               SELECT new ru.practicum.stats.dto.ViewStatsDto(eh.app, eh.uri, COUNT(eh.ip))
               FROM EndpointHit as eh
               WHERE eh.timestamp BETWEEN :start AND :end
               GROUP BY eh.app, eh.uri
               ORDER BY COUNT(eh.ip) DESC
            """)
    List<ViewStatsDto> findNoUniqueAndNoUrisStats(LocalDateTime start, LocalDateTime end);

    @Query("""
               SELECT new ru.practicum.stats.dto.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip))
               FROM EndpointHit as eh
               WHERE eh.uri IN :uris AND eh.timestamp BETWEEN :start AND :end
               GROUP BY eh.app, eh.uri
               ORDER BY COUNT(eh.ip) DESC
            """)
    List<ViewStatsDto> findUniqueWithUrisStats(
        @Param("uris") List<String> uris,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
               SELECT new ru.practicum.stats.dto.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip))
               FROM EndpointHit as eh
               WHERE eh.timestamp BETWEEN :start AND :end
               GROUP BY eh.app, eh.uri
               ORDER BY COUNT(eh.ip) DESC
            """)
    List<ViewStatsDto> findUniqueAndNoUrisStats(LocalDateTime start, LocalDateTime end);

    @Query("""
               SELECT new ru.practicum.stats.dto.ViewStatsDto(eh.app, eh.uri, COUNT(eh.ip))
               FROM EndpointHit as eh
               WHERE eh.uri IN :uris AND eh.timestamp BETWEEN :start AND :end
               GROUP BY eh.app, eh.uri
               ORDER BY COUNT(eh.ip) DESC
            """)
    List<ViewStatsDto> findNoUniqueWithUrisStats(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(eh) > 0 FROM EndpointHit eh WHERE eh.uri = :uri AND eh.ip = :ip")
    boolean existsByUriAndIp(@Param("uri") String uri, @Param("ip") String ip);
}