package ru.practicum.explore_with_me.event_category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.Column;

@Data
@AllArgsConstructor
public class CategoryDto {
    private int id;
    @NonNull
    @Column(unique = true)
    private String name;
}
