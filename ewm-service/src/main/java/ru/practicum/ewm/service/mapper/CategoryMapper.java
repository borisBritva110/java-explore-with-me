package ru.practicum.ewm.service.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.service.dto.CategoryDto;
import ru.practicum.ewm.service.dto.NewCategoryDto;
import ru.practicum.ewm.service.model.Category;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
            .name(newCategoryDto.getName())
            .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
            .id(category.getId())
            .name(category.getName())
            .build();
    }
}