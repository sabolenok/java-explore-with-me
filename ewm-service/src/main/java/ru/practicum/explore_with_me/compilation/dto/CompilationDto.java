package ru.practicum.explore_with_me.compilation.dto;

import lombok.Data;

@Data
public class CompilationDto {
    private int id;
    private boolean isPinned;
    private String title;
}
