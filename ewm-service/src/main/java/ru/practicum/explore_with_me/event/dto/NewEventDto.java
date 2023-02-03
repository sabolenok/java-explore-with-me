package ru.practicum.explore_with_me.event.dto;

import lombok.Data;
import ru.practicum.explore_with_me.event.Location;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.user.User;

import java.time.LocalDateTime;

@Data
public class NewEventDto {
    private String description;
    private String annotation;
    private int categoryId;
    private Category category;
    private LocalDateTime eventDate;
    private int initiatorId;
    private User initiator;
    private boolean isPaid;
    private String title;
    private int participantLimit;
    private boolean requestModeration;
    private int locationId;
    private Location location;
}
