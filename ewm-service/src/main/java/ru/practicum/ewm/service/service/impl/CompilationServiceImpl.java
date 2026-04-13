package ru.practicum.ewm.service.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.service.dto.CompilationDto;
import ru.practicum.ewm.service.dto.NewCompilationDto;
import ru.practicum.ewm.service.dto.UpdateCompilationRequest;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.mapper.CompilationMapper;
import ru.practicum.ewm.service.model.Compilation;
import ru.practicum.ewm.service.model.Event;
import ru.practicum.stats.dto.NotFound;
import ru.practicum.ewm.service.repository.CompilationRepository;
import ru.practicum.ewm.service.service.CompilationService;
import ru.practicum.ewm.service.service.EventService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Получаем подборки - pinned: {}, from: {}, size: {}", pinned, from, size);

        int pageNumber = from / size;
        PageRequest pageable = PageRequest.of(pageNumber, size);
        List<Compilation> compilations = compilationRepository.findAllWithFilters(pinned, pageable).getContent();

        List<Event> allEvents = compilations.stream()
            .flatMap(compilation -> compilation.getEvents().stream())
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Long> viewsMap = eventService.getViewStatsForEvents(allEvents);

        return compilations.stream()
            .map(comp -> CompilationMapper.toCompilationDto(comp, viewsMap))
            .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получаем подборку id {}", compId);

        Compilation compilation = getCompilationOrThrow(
            compilationRepository.findByIdWithEvents(compId), compId);

        Map<Long, Long> viewsMap = eventService.getViewStatsForEvents(
            compilation.getEvents().stream().toList());

        return CompilationMapper.toCompilationDto(compilation, viewsMap);
    }

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Добавляем подборку: {}", newCompilationDto);
        Set<Long> newEvents = newCompilationDto.getEvents();
        Set<Event> events = new HashSet<>();
        if (newEvents != null && !newEvents.isEmpty()) {
            events = new HashSet<>(eventService.getAllEventById(newEvents));
        }

        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, events);
        compilation = compilationRepository.save(compilation);
        log.info("Добавлена подборка: {}", compilation);

        Map<Long, Long> viewsMap = eventService.getViewStatsForEvents(
            compilation.getEvents().stream().toList());

        return CompilationMapper.toCompilationDto(compilation, viewsMap);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки id: {}", compId);
        compilationRepository.deleteById(compId);
        log.info("Удалена подборка id: {}", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Обновляем подборку id {}: {}", compId, updateRequest);

        Compilation compilation = getCompilationOrThrow(
            compilationRepository.findById(compId), compId);

        if (updateRequest.hasTitle()) {
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.hasPinned()) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.hasEvents()) {
            Set<Event> events = new HashSet<>(
                eventService.getAllEventById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        compilation = compilationRepository.save(compilation);
        log.info("Обновлена подборка: {}", compilation);

        Map<Long, Long> viewsMap = eventService.getViewStatsForEvents(
            compilation.getEvents().stream().toList());

        return CompilationMapper.toCompilationDto(compilation, viewsMap);
    }

    private Compilation getCompilationOrThrow(Optional<Compilation> compOpt, Long compId) {
        return compOpt
            .orElseThrow(() -> new NotFoundException(
                String.format(NotFound.COMPILATION, compId)));
    }
}