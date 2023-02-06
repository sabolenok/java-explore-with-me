package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event_request.RequestMapper;
import ru.practicum.explore_with_me.event_request.RequestService;
import ru.practicum.explore_with_me.event_request.dto.EventRequestDto;
import ru.practicum.explore_with_me.event_request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.event_request.dto.EventRequestStatusUpdateResult;

import java.util.List;
import java.util.stream.Collectors;


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

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public EventRequestDto cancel(@PathVariable Integer userId, @PathVariable Integer requestId) {
        return RequestMapper.toEventRequestDto(requestService.cancel(userId, requestId));
    }

    @GetMapping("/users/{userId}/requests")
    public List<EventRequestDto> getAll(@PathVariable Integer userId,
                                      @RequestParam(required = false, defaultValue = "0") Integer from,
                                      @RequestParam(required = false, defaultValue = "10") Integer size) {
        return requestService.getByUser(userId, from, size)
                .stream().map(RequestMapper::toEventRequestDto).collect(Collectors.toList());
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult put(@PathVariable Integer userId, @PathVariable Integer eventId,
                                              @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        return requestService.updateRequests(userId, eventId, statusUpdateRequest);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<EventRequestDto> getByUserAndEvent(@PathVariable Integer userId, @PathVariable Integer eventId,
                                                   @RequestParam(required = false, defaultValue = "0") Integer from,
                                                   @RequestParam(required = false, defaultValue = "10") Integer size) {
        return requestService.getByUserAndEvent(userId, eventId, from, size)
                .stream().map(RequestMapper::toEventRequestDto).collect(Collectors.toList());
    }
}
