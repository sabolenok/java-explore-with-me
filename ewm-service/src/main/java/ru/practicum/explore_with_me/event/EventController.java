package ru.practicum.explore_with_me.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event.dto.EventShortDto;
import ru.practicum.explore_with_me.event.dto.NewEventDto;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventController {

    public final EventService eventService;

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventShortDto add(@PathVariable Integer userId, @RequestBody NewEventDto eventDto) {
        return EventMapper.toEventShortDto(eventService.create(EventMapper.toEvent(eventDto), userId));
    }
}
