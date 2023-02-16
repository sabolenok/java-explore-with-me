package ru.practicum.explore_with_me.event.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private int id;
    private String text;
    private LocalDateTime created;
    private Integer eventId;
    private Integer authorId;
}
