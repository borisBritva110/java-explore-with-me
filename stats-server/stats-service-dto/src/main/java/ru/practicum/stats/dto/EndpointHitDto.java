package ru.practicum.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EndpointHitDto {

    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private String app;

    @NotBlank(message = "URI не может быть пустым")
    private String uri;

    @NotBlank(message = "IP-адресс не может быт пустым")
    private String ip;

    @NotNull(message = "Временная отметка запроса к эндпоинту не может быть пустой")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}