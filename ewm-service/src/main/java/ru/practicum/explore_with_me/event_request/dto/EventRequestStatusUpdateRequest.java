package ru.practicum.explore_with_me.event_request.dto;

import lombok.Data;

@Data
public class EventRequestStatusUpdateRequest {
    private Integer[] requestIds;
    private EventRequestStateAction status;
}
