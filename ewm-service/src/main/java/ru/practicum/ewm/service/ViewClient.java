package ru.practicum.ewm.service;

import java.io.UnsupportedEncodingException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.stats.client.StatsClient;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ViewClient {
    private final StatsClient statsClient;

    public Integer getViews(HttpServletRequest request) throws UnsupportedEncodingException {
        ResponseEntity<Object> response = statsClient.getViews(request.getRequestURI());
        Integer views = null;
        if (response.getBody() instanceof Integer) {
            views = (Integer) response.getBody();
        } else if (response.getBody() instanceof Long) {
            views = (Integer) (response.getBody());
        }
        return views;
    }
}