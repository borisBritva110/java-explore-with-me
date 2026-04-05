package ru.practicum.ewm.service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.CategoryDto;
import ru.practicum.ewm.service.dto.NewCategoryDto;
import ru.practicum.ewm.service.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории: {}", newCategoryDto);
        CategoryDto categoryDto = categoryService.addCategory(newCategoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryDto);
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        log.info("Удаление категории с id={}", catId);
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Изменение категории с id={}: {}", catId, categoryDto);
        CategoryDto updatedCategory = categoryService.updateCategory(catId, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }
}