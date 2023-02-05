package ru.practicum.explore_with_me.event_request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.event.Event;
import ru.practicum.explore_with_me.event.EventRepository;
import ru.practicum.explore_with_me.event.dto.EventState;
import ru.practicum.explore_with_me.exception.EventOwnerException;
import ru.practicum.explore_with_me.exception.EventStatusException;
import ru.practicum.explore_with_me.exception.NotFoundException;
import ru.practicum.explore_with_me.user.User;
import ru.practicum.explore_with_me.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RequestService {

    @Getter
    private final RequestRepository repository;

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    @Transactional
    public EventRequest create(Integer userId, Integer eventId) {

        Optional<EventRequest> foundRequest = repository.findByRequesterIdAndEventId(userId, eventId);
        if (foundRequest.isPresent()) {
            throw new EventOwnerException(String.format("Request to event %s from user %s already exists.", eventId, userId),
                    "Integrity constraint has been violated.");
        }

        Optional<Event> foundEvent = eventRepository.findById(eventId);
        if (foundEvent.isPresent()) {
            Event event = foundEvent.get();
            if (event.getInitiatorId().equals(userId)) {
                throw new EventOwnerException(String.format("User %s is owner for event %s.", userId, eventId),
                        "Integrity constraint has been violated.");
            }

            if (!event.getState().equals(EventState.PUBLISHED)) {
                throw new EventStatusException(String.format("Event %s is not published yet.", eventId),
                        "Integrity constraint has been violated.");
            }

            if (event.getConfirmedRequests() != null & event.getParticipantLimit() != null
                    && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new EventStatusException(String.format("Event %s request limit exceeded.", eventId),
                        "Integrity constraint has been violated.");
            }

            Optional<User> foundUser = userRepository.findById(userId);
            if (foundUser.isEmpty()) {
                throw new NotFoundException(String.format("User with id=%d was not found", userId),
                        "The required object was not found.");
            }
            User requester = foundUser.get();
            EventRequest request = new EventRequest();

            request.setRequesterId(userId);
            request.setRequester(requester);
            request.setEventId(eventId);
            request.setEvent(event);
            request.setCreated(LocalDateTime.now());
            request.setStatus(event.getRequestModeration() ? EventRequestState.PENDING : EventRequestState.PUBLISHED);

            if (request.getStatus().equals(EventRequestState.PUBLISHED)) {
                event.setConfirmedRequests(event.getConfirmedRequests()+1);
                eventRepository.save(event);
            }

            return repository.save(request);
        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId),
                    "The required object was not found.");
        }
    }

    @Transactional
    public EventRequest cancel(Integer userId, Integer requestId) {

        Optional<EventRequest> foundRequest = repository.findById(requestId);
        if (foundRequest.isEmpty()) {
            throw new NotFoundException(String.format("Request with id=%d was not found", requestId),
                    "The required object was not found.");
        } else {
            EventRequest request = foundRequest.get();

            Optional<Event> foundEvent = eventRepository.findById(request.getEventId());
            if (foundEvent.isPresent()) {
                Event event = foundEvent.get();
                Optional<User> foundUser = userRepository.findById(userId);
                if (foundUser.isEmpty()) {
                    throw new NotFoundException(String.format("User with id=%d was not found", userId),
                            "The required object was not found.");
                }

                if (request.getStatus().equals(EventRequestState.PUBLISHED)) {
                    event.setConfirmedRequests(event.getConfirmedRequests()-1);
                    eventRepository.save(event);
                }

                request.setStatus(EventRequestState.CANCELED);

                return repository.save(request);
            } else {
                throw new NotFoundException(String.format("Event with id=%d was not found", request.getEventId()),
                        "The required object was not found.");
            }
        }
    }
}
