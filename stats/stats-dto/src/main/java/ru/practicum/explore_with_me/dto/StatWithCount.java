package ru.practicum.explore_with_me.dto;

import lombok.Data;

@Data
public class StatWithCount {
    private String app;
    private String uri;
    private Integer hits;
}
