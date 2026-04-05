package ru.practicum.ewm.service.service;

import ru.practicum.ewm.service.dto.CompilationDto;
import ru.practicum.ewm.service.dto.NewCompilationDto;
import ru.practicum.ewm.service.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    void deleteCompilation(Long compId);
}