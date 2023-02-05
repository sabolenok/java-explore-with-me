package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event_request.RequestMapper;
import ru.practicum.explore_with_me.event_request.RequestService;
import ru.practicum.explore_with_me.event_request.dto.EventRequestDto;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public EventRequestDto add(@PathVariable Integer userId, @RequestParam Integer eventId) {
        return RequestMapper.toEventRequestDto(requestService.create(userId, eventId));
    }
}
