package ru.practicum.explore_with_me.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatWithCount {
    private String app;
    private String uri;
    private Integer hits;
}
