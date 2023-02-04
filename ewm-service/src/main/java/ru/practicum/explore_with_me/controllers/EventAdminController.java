package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event.EventMapper;
import ru.practicum.explore_with_me.event.EventService;
import ru.practicum.explore_with_me.event.dto.EventFullDto;
import ru.practicum.explore_with_me.event.dto.UpdateEventAdminRequestDto;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventAdminController {

    public final EventService eventService;

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto put(@PathVariable Integer eventId,
                            @RequestBody UpdateEventAdminRequestDto eventDto) {
        return EventMapper.toEventFullDto(eventService.putByAdmin(EventMapper.toEvent(eventDto), eventId, eventDto.getStateAction()));
    }
}
