package ru.practicum.ewm.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewCompilationDto {
    private Set<Long> events;
    private Boolean pinned;

    @NotBlank(message = "Field: title. Error: must not be blank")
    @Size(min = 1, max = 50, message = "Field: title. Error: длина должна быть от 1 до 50 символов")
    private String title;
}