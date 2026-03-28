package ru.practicum.stats.service.mapper;

import java.time.Instant;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.service.model.EndpointHit;
import ru.practicum.stats.service.util.DateTime;

public class EndpointHitMapper {

    public static EndpointHit toEntity(EndpointHitDto dto) {
        EndpointHit entity = new EndpointHit();
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(dto.getIp());
        entity.setTimestamp(dto.getTimestamp());
        return entity;
    }
}