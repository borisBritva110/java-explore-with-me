package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class StatsClient {
    @Value("${stats-service.url}")
    private String url;
    private final RestTemplate restTemplate;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient() {
        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto endpointHitDto) {
        log.info("Вызываем метод saveHit из clientService {}", endpointHitDto);
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url.concat("/hit"),
                endpointHitDto, Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.hasBody() ? response.getBody() : null);
        } catch (HttpStatusCodeException e) {
            log.warn("HttpStatusCodeException method saveHit {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    public ResponseEntity<Object> getStats(
        LocalDateTime start,
        LocalDateTime end,
        List<String> uris,
        Boolean unique
    ) {
        StringBuilder urlResult = new StringBuilder(url.concat("/stats?"));

        for (String str : uris) {
            urlResult.append("uris=").append(str).append("&");
        }
        urlResult.append("start=").append(start.format(formatter));
        urlResult.append("&end=").append(end.format(formatter)).append("&");
        urlResult.append("unique=").append(unique);

        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(urlResult.toString(), Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.hasBody() ? response.getBody() : null);
        } catch (HttpStatusCodeException e) {
            log.warn("HttpStatusCodeException method getStats {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }
}