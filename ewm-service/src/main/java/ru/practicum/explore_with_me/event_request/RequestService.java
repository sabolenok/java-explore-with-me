package ru.practicum.explore_with_me.event_request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.event.Event;
import ru.practicum.explore_with_me.event.EventRepository;
import ru.practicum.explore_with_me.event.dto.EventState;
import ru.practicum.explore_with_me.event_request.dto.EventRequestDto;
import ru.practicum.explore_with_me.event_request.dto.EventRequestStateAction;
import ru.practicum.explore_with_me.event_request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.event_request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore_with_me.exception.EventOwnerException;
import ru.practicum.explore_with_me.exception.EventStatusException;
import ru.practicum.explore_with_me.exception.NotFoundException;
import ru.practicum.explore_with_me.user.User;
import ru.practicum.explore_with_me.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

            int confirmedRequests = repository.findByEventIdAndStatus(eventId, EventRequestState.CONFIRMED).size();
            int participantLimit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
            if (confirmedRequests >= participantLimit) {
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
            request.setStatus(event.getRequestModeration() ? EventRequestState.PENDING : EventRequestState.CONFIRMED);

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
                Optional<User> foundUser = userRepository.findById(userId);
                if (foundUser.isEmpty()) {
                    throw new NotFoundException(String.format("User with id=%d was not found", userId),
                            "Integrity constraint has been violated.");
                }

                if (request.getRequesterId() != userId) {
                    throw new EventOwnerException(String.format("User with id=%d is not requester", userId),
                            "The required object was not found.");
                }

                request.setStatus(EventRequestState.CANCELED);

                return repository.save(request);
            } else {
                throw new NotFoundException(String.format("Event with id=%d was not found", request.getEventId()),
                        "The required object was not found.");
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<EventRequest> getByUser(int userId, int from, int size) {

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        return repository.findAllByRequesterId(userId, PageRequest.of(from / size, size));
    }

    @Transactional(readOnly = true)
    public Page<EventRequest> getByUserAndEvent(int userId, int eventId, int from, int size) {

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        Optional<Event> foundEvent = eventRepository.findById(eventId);
        if (foundEvent.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId),
                    "The required object was not found.");
        }

        if (!foundEvent.get().getInitiatorId().equals(userId)) {
            throw new EventOwnerException(String.format("User %s is owner for event %s.", userId, eventId),
                    "Integrity constraint has been violated.");
        }

        return repository.findAllByEventId(eventId, PageRequest.of(from / size, size));
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequests(int userId, int eventId,
                                                         EventRequestStatusUpdateRequest requestsResult) {
        Optional<Event> foundEvent = eventRepository.findById(eventId);
        if (foundEvent.isPresent()) {
            Event event = foundEvent.get();

            if ((event.getParticipantLimit() == null || event.getParticipantLimit() == 0)
                || (event.getRequestModeration() != null && !event.getRequestModeration())) {
                return null;
            }

            Optional<User> foundUser = userRepository.findById(userId);
            if (foundUser.isEmpty()) {
                throw new NotFoundException(String.format("User with id=%d was not found", userId),
                        "The required object was not found.");
            }

            if (!event.getInitiatorId().equals(userId)) {
                throw new EventOwnerException(String.format("User %s is owner for event %s.", userId, eventId),
                        "Integrity constraint has been violated.");
            }

            int confirmedRequests = repository.findByEventIdAndStatus(eventId, EventRequestState.CONFIRMED).size();
            int participantLimit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
            if (confirmedRequests >= participantLimit) {
                throw new EventStatusException("The participant limit has been reached",
                        "For the requested operation the conditions are not met.");
            }

            List<EventRequest> requests = repository.findByEventIdAndIdInOrderById(eventId, List.of(requestsResult.getRequestIds()));
            EventRequestStateAction stateAction = requestsResult.getStatus();
            for (EventRequest request : requests) {
                if (!request.getStatus().equals(EventRequestState.PENDING)) {
                    throw new EventStatusException(String.format("Request %s is not in pending status.", request.getId()),
                            "For the requested operation the conditions are not met.");
                }
                if (stateAction.equals(EventRequestStateAction.CONFIRMED)) {
                    if (confirmedRequests < participantLimit) {
                        request.setStatus(EventRequestState.CONFIRMED);
                        confirmedRequests++;
                    } else {
                        request.setStatus(EventRequestState.REJECTED);
                    }
                } else {
                    request.setStatus(EventRequestState.REJECTED);
                }
            }
            repository.saveAll(requests);

            List<EventRequestDto> confirmed = requests.stream()
                    .filter(x -> x.getStatus().equals(EventRequestState.CONFIRMED))
                    .map(RequestMapper::toEventRequestDto).collect(Collectors.toList());
            List<EventRequestDto> rejected = requests.stream()
                    .filter(x -> x.getStatus().equals(EventRequestState.REJECTED))
                    .map(RequestMapper::toEventRequestDto).collect(Collectors.toList());
            return new EventRequestStatusUpdateResult(confirmed, rejected);
        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId),
                    "The required object was not found.");
        }
    }
}
