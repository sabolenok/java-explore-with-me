package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event_category.CategoryMapper;
import ru.practicum.explore_with_me.event_category.CategoryService;
import ru.practicum.explore_with_me.event_category.dto.CategoryDto;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CategoryAdminController {

    public final CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto add(@RequestBody CategoryDto categoryDto) {
        return CategoryMapper.toCategoryDto(categoryService.create(CategoryMapper.toCategory(categoryDto)));
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto patch(@PathVariable Integer catId, @RequestBody CategoryDto categoryDto) {
        return CategoryMapper.toCategoryDto(categoryService.put(catId, CategoryMapper.toCategory(categoryDto)));
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer catId) {
        categoryService.delete(catId);
    }
}
