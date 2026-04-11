package ru.practicum.ewm.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class NewEventDto {

    @NotBlank(message = "Field: annotation. Error: must not be blank")
    @Size(min = 20, max = 2000, message = "Field: annotation. Error: длина должна быть от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "Field: category. Error: must not be blank")
    private Long category;

    @NotBlank(message = "Field: description. Error: must not be blank")
    @Size(min = 20, max = 7000, message = "Field: description. Error: длина должна быть от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Field: eventDate. Error: must not be blank")
    @Future(message = "Field: eventDate. Error: должно содержать дату, которая еще не наступила")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Field: location. Error: must not be blank")
    @Valid
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Field: participantLimit. Error: must be positive or zero")
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Field: title. Error: must not be blank")
    @Size(min = 3, max = 120, message = "Field: title. Error: длина должна быть от 3 до 120 символов")
    private String title;
}