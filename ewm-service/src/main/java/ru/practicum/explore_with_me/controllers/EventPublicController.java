package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.event.EventMapper;
import ru.practicum.explore_with_me.event.EventService;
import ru.practicum.explore_with_me.event.dto.EventFullDto;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventPublicController {

    public final EventService eventService;

    @GetMapping("/events/{id}")
    public EventFullDto get(@PathVariable Integer id) {
        return EventMapper.toEventFullDto(eventService.getPublished(id));
    }
}
