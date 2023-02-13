package ru.practicum.explore_with_me.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventShortDto {
    private int id;
    private String annotation;
    private Category category;
    private int confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private boolean paid;
    private String title;
    private int views;
    private List<CommentDto> comments;
}
