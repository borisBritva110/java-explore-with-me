package ru.practicum.ewm.service.mapper;

import ru.practicum.ewm.service.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.dto.RequestStatus;
import ru.practicum.ewm.service.model.ParticipationRequest;

public final class RequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest entity) {
        return ParticipationRequestDto.builder()
            .id(entity.getId())
            .created(entity.getCreated())
            .event(entity.getEvent().getId())
            .requester(entity.getRequester().getId())
            .status(RequestStatus.valueOf(entity.getStatus().name()))
            .build();
    }
}