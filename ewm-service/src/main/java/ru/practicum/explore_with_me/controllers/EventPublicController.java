package ru.practicum.explore_with_me.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event.EventMapper;
import ru.practicum.explore_with_me.event.EventService;
import ru.practicum.explore_with_me.event.dto.EventFullDto;
import ru.practicum.explore_with_me.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventPublicController {

    public final EventService eventService;

    @GetMapping("/events/{id}")
    public EventFullDto get(@PathVariable Integer id,
                            HttpServletRequest request) throws JsonProcessingException {
        return EventMapper.toEventFullDto(eventService.getPublished(id, request));
    }

    @GetMapping("/events")
    public List<EventShortDto> get(@RequestParam(required = false) String text,
                                       @RequestParam(required = false) Integer[] categories,
                                       @RequestParam(required = false) Boolean paid,
                                       @RequestParam(required = false) String rangeStart,
                                       @RequestParam(required = false) String rangeEnd,
                                       @RequestParam(required = false) Boolean onlyAvailable,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(required = false, defaultValue = "0") Integer from,
                                       @RequestParam(required = false, defaultValue = "10") Integer size,
                                   HttpServletRequest request) throws JsonProcessingException {
        return eventService
                .getAllForPublicWithFilters(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                        sort, from, size, request)
                .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }
}
