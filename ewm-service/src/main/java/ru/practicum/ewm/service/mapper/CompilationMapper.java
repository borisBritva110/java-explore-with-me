package ru.practicum.ewm.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.dto.CompilationDto;
import ru.practicum.ewm.service.dto.NewCompilationDto;
import ru.practicum.ewm.service.model.Compilation;
import ru.practicum.ewm.service.model.Event;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {
    public static final Boolean PINNED_DEFAULT_VALUE = false;

    public static Compilation toCompilation(NewCompilationDto newCompilationDto, Set<Event> events) {
        return Compilation.builder()
            .pinned(newCompilationDto
                .getPinned() != null ? newCompilationDto.getPinned() : PINNED_DEFAULT_VALUE)
            .title(newCompilationDto.getTitle())
            .events(events != null ? events : Set.of())
            .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation,
                                                  Map<Long, Long> viewsMap) {
        return CompilationDto.builder()
            .id(compilation.getId())
            .pinned(compilation.getPinned())
            .title(compilation.getTitle())
            .events(compilation.getEvents().stream()
                .map(event -> EventMapper.toEventShortDto(
                    event, viewsMap
                        .getOrDefault(event.getId(), EventMapper.NO_VIEWS)))
                .collect(Collectors.toList()))
            .build();
    }
}