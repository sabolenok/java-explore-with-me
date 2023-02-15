package ru.practicum.explore_with_me.event_request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.explore_with_me.event_request.EventRequestState;

import java.time.LocalDateTime;

@Data
public class EventRequestDto {
    private int id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private int event;
    private int requester;
    private EventRequestState status;
}
