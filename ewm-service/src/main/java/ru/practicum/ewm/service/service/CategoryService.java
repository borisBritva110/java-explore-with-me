package ru.practicum.ewm.service.service;

import ru.practicum.ewm.service.dto.CategoryDto;
import ru.practicum.ewm.service.dto.NewCategoryDto;
import ru.practicum.ewm.service.model.Category;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    void deleteCategory(Long catId);

    List<CategoryDto> getCategories(int from, int size);

    Category getCategoryById(Long catId);

    CategoryDto getCategoryDtoById(Long catId);
}