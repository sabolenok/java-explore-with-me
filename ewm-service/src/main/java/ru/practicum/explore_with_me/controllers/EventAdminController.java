package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event.EventMapper;
import ru.practicum.explore_with_me.event.EventService;
import ru.practicum.explore_with_me.event.dto.EventFullDto;
import ru.practicum.explore_with_me.event.dto.UpdateEventAdminRequestDto;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/admin/events")
    public List<EventFullDto> get(@RequestParam(required = false) Integer[] users,
                                  @RequestParam(required = false) String[] states,
                                  @RequestParam(required = false) Integer[] categories,
                                  @RequestParam(required = false) String rangeStart,
                                  @RequestParam(required = false) String rangeEnd,
                                  @RequestParam(required = false, defaultValue = "0") Integer from,
                                  @RequestParam(required = false, defaultValue = "10") Integer size) {
        return eventService.getAllForAdminWithFilters(users, states, categories, rangeStart, rangeEnd, from, size)
                .stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }
}
