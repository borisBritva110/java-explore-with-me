package ru.practicum.ewm.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
@Setter
public class Location {

    @Column(name = "location_lat", nullable = false)
    private Float lat;

    @Column(name = "location_lon", nullable = false)
    private Float lon;
}