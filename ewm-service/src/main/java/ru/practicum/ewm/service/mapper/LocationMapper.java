package ru.practicum.ewm.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.dto.LocationDto;
import ru.practicum.ewm.service.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationMapper {
    public static Location toLocation(LocationDto locationDto) {
        return Location.builder()
            .lat(locationDto.getLat())
            .lon(locationDto.getLon())
            .build();
    }

    public static LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
            .lat(location.getLat())
            .lon(location.getLon())
            .build();
    }
}