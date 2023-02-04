package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event_category.CategoryMapper;
import ru.practicum.explore_with_me.event_category.CategoryService;
import ru.practicum.explore_with_me.event_category.dto.CategoryDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CategoryPublicController {

    public final CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryDto> getAll(@RequestParam(required = false, defaultValue = "0") Integer from,
                                    @RequestParam(required = false, defaultValue = "100") Integer size) {
        return categoryService.getAll(from, size)
                .stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto get(@PathVariable Integer catId) {
        return CategoryMapper.toCategoryDto(categoryService.getById(catId));
    }
}
