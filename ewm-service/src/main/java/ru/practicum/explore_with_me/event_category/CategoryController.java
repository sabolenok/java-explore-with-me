package ru.practicum.explore_with_me.event_category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event_category.dto.CategoryDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CategoryController {

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
