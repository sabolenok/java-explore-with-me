package ru.practicum.explore_with_me.event_category;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.event_category.dto.CategoryDto;

@Component
public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public static Category toCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.getId());
        category.setName(categoryDto.getName());
        return category;
    }
}
