package ru.practicum.explore_with_me.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.explore_with_me.event.Location;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    private int id;
    private String description;
    private String annotation;
    private Category category;
    private int confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private boolean paid;
    private String title;
    private int views;
    private int participantLimit;
    private boolean requestModeration;
    private Location location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    private EventState state;
}
