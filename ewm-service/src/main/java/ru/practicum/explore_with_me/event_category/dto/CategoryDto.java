package ru.practicum.explore_with_me.event_category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class CategoryDto {
    private int id;
    @NonNull
    private String name;
}
