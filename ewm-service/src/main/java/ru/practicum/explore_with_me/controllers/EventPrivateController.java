package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event.EventMapper;
import ru.practicum.explore_with_me.event.EventService;
import ru.practicum.explore_with_me.event.dto.EventFullDto;
import ru.practicum.explore_with_me.event.dto.EventShortDto;
import ru.practicum.explore_with_me.event.dto.NewEventDto;
import ru.practicum.explore_with_me.event.dto.UpdateEventUserRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventPrivateController {

    public final EventService eventService;

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable Integer userId, @Valid @RequestBody NewEventDto eventDto) {
        return EventMapper.toEventFullDto(eventService.create(EventMapper.toEvent(eventDto), userId));
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto put(@PathVariable Integer userId, @PathVariable Integer eventId,
                            @RequestBody UpdateEventUserRequestDto eventDto) {
        return EventMapper.toEventFullDto(eventService.putByUser(EventMapper.toEvent(eventDto), userId, eventId, eventDto.getStateAction()));
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto get(@PathVariable Integer userId, @PathVariable Integer eventId) {
        return EventMapper.toEventFullDto(eventService.get(userId, eventId));
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getAll(@PathVariable Integer userId,
                                      @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
                                      @Positive @RequestParam(required = false, defaultValue = "10") Integer size) {
        return eventService.getAll(userId, from, size)
                .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }
}
