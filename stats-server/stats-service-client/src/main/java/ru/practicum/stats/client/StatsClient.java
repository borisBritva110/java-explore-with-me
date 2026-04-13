package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatsClient {
    @Value("${stats-server.url:http://stats-server:9090}")
    private String url;
    private final RestTemplate restTemplate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public StatsClient() {
        this.restTemplate = new RestTemplate();
    }

    public void saveHit(EndpointHitDto endpointHitDto) {
        log.info("Вызываем метод saveHit из clientService {}", endpointHitDto);
            try {
                ResponseEntity<Object> response = restTemplate.postForEntity(url.concat("/hit"),
                    endpointHitDto, Object.class);
                ResponseEntity.status(response.getStatusCode()).body(response.hasBody() ? response.getBody() : null);
            } catch (HttpStatusCodeException e) {
                log.warn("HttpStatusCodeException method saveHit {}", e.getMessage());
                ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
            } catch (Exception e) {
                log.warn("Exception method saveHit {}", e.getMessage());
                ResponseEntity.status(500).body(null);
            }
        }

    public List<ViewStatsDto> getStats(
        LocalDateTime start,
        LocalDateTime end,
        List<String> uris,
        Boolean unique) {
        StringBuilder urlResult = new StringBuilder(url.concat("/stats?"));

        for (String str : uris) {
            urlResult.append("uris=").append(str).append("&");
        }
        urlResult.append("start=").append(start.format(formatter));
        urlResult.append("&end=").append(end.format(formatter)).append("&");
        urlResult.append("unique=").append(unique);

        try {
            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                urlResult.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (HttpStatusCodeException e) {
            log.warn("HttpStatusCodeException method getStats {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Exception method getStats {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}