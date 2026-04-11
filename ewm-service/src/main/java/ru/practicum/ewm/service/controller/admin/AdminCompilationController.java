package ru.practicum.ewm.service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.CompilationDto;
import ru.practicum.ewm.service.dto.NewCompilationDto;
import ru.practicum.ewm.service.dto.UpdateCompilationRequest;
import ru.practicum.ewm.service.service.CompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
@Validated
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> saveCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Добавление новой подборки: {}", newCompilationDto);
        CompilationDto compilationDto = compilationService.addCompilation(newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(compilationDto);
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        log.info("Удаление подборки с id={}", compId);
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        log.info("Обновление подборки с id={}: {}", compId, updateRequest);
        CompilationDto compilationDto = compilationService.updateCompilation(compId, updateRequest);
        return ResponseEntity.ok(compilationDto);
    }
}