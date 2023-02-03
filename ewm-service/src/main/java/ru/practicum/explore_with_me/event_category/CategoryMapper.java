package ru.practicum.explore_with_me.event_category;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.event_category.dto.CategoryDto;

@Component
public class CategoryMapper {
    private static final ModelMapper modelMapper = new ModelMapper();

    public static CategoryDto toCategoryDto(Category category) {
        return modelMapper.map(category, CategoryDto.class);
    }

    public static Category toCategory(CategoryDto categoryDto) {
        return modelMapper.map(categoryDto, Category.class);
    }
}
