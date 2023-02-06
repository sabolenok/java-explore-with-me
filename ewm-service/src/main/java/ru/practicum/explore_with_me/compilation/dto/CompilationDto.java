package ru.practicum.explore_with_me.compilation.dto;

import lombok.Data;
import ru.practicum.explore_with_me.event.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {
    private int id;
    private boolean pinned;
    private String title;
    private List<EventShortDto> eventsObj;
}
