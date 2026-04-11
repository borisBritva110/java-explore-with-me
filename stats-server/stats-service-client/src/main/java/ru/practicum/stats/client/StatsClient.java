package ru.practicum.stats.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class StatsClient {
    private static final Logger log = LoggerFactory.getLogger(StatsClient.class);

    private String url;
    private final RestClient restClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";

    public StatsClient(String url) {
        this.url = url;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setConnectionRequestTimeout(Duration.ofSeconds(5));

        restClient = RestClient.builder()
            .requestFactory(factory)
            .baseUrl(url)
            .build();
    }

    public void saveHit(EndpointHitDto hitDto) {

        try {
            restClient.post()
                .uri(HIT_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();

        } catch (Exception e) {
            log.error("Unexpected error in hit(): {}", e.getMessage());
            throw new RuntimeException("Failed to save hit: " + e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                            LocalDateTime end,
                                            Collection<String> uris,
                                            boolean unique) {
        String serverUrl = UriComponentsBuilder.fromHttpUrl(url + STATS_ENDPOINT)
            .queryParam("start", start.format(formatter))
            .queryParam("end", end.format(formatter))
            .queryParam("uris", uris)
            .queryParam("unique", unique)
            .toUriString();

        log.debug("Requesting stats from: {}", url);

        try {
            ViewStatsDto[] stats = restClient.get()
                .uri(serverUrl)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("Stats service error: " + res.getStatusCode());
                })
                .body(ViewStatsDto[].class);
            return stats != null ? Arrays.asList(stats) : Collections.emptyList();
        } catch (ResourceAccessException e) {
            log.error("Stats service unavailable. URL: {}, error: {}", serverUrl, e.getMessage());
            throw new RuntimeException("Connection to stats service failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in getStats(): {}", e.getMessage());
            throw new RuntimeException("Failed to get stats: " + e.getMessage());
        }
    }
}