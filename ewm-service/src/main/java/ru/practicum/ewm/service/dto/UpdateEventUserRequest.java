package ru.practicum.ewm.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Field: annotation. Error: длина должна быть от 20 до 2000 символов")
    private String annotation;

    @PositiveOrZero(message = "Field: category. Error: must be positive")
    private Long category;

    @Size(min = 20, max = 7000, message = "Field: description. Error: длина должна быть от 20 до 7000 символов")
    private String description;

    @Future(message = "Field: eventDate. Error: должно содержать дату, которая еще не наступила")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Valid
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Field: participantLimit. Error: must be positive or zero")
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "Field: title. Error: длина должна быть от 3 до 120 символов")
    private String title;

    public boolean hasAnnotation() {
        return annotation != null;
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasEventDate() {
        return eventDate != null;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasPaid() {
        return paid != null;
    }

    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    public boolean hasStateAction() {
        return stateAction != null;
    }

    public boolean hasTitle() {
        return title != null;
    }
}