package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.event.dto.EventState;
import ru.practicum.explore_with_me.event.dto.StateAction;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.event_category.CategoryRepository;
import ru.practicum.explore_with_me.exception.EventDateException;
import ru.practicum.explore_with_me.exception.EventStatusException;
import ru.practicum.explore_with_me.exception.NotFoundException;
import ru.practicum.explore_with_me.exception.UnsupportedStateException;
import ru.practicum.explore_with_me.user.User;
import ru.practicum.explore_with_me.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EventService {

    @Getter
    private final EventRepository repository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    @Transactional
    public Event create(Event event, Integer userId) {
        checkEventDate(event);

        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Optional<Category> category = categoryRepository.findById(event.getCategoryId());
        category.ifPresent(event::setCategory);

        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            event.setInitiator(user.get());
            event.setInitiatorId(userId);

            return repository.save(event);
        } else {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }
    }

    @Transactional(readOnly = true)
    public Event get(int userId, int eventId) {

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        Optional<Event> foundEvent = repository.findById(eventId);
        if (foundEvent.isPresent()) {
            Event event = foundEvent.get();
            if (!event.getInitiatorId().equals(userId)) {
                 throw new NotFoundException(String.format("Event with id=%d was not found", eventId),
                            "The required object was not found.");
            }
            event.setInitiator(foundUser.get());

            Optional<Category> category = categoryRepository.findById(event.getCategoryId());
            category.ifPresent(event::setCategory);

            return event;
        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId),
                    "The required object was not found.");
        }
    }

    @Transactional(readOnly = true)
    public Page<Event> getAll(int userId, int from, int size) {

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        Page<Event> events = repository.findByInitiatorId(userId, PageRequest.of(from / size, size));
        Set<Integer> EventsCategories = new HashSet<>();
        for (Event event : events) {
            event.setInitiator(foundUser.get());
            EventsCategories.add(event.getCategoryId());
        }

        if (!EventsCategories.isEmpty()) {
            Map<Integer, Category> categories = categoryRepository.findAllById(EventsCategories)
                    .stream().collect(Collectors.toMap(Category::getId, category -> category));
            for (Event event : events) {
                if (categories.containsKey(event.getCategoryId())) {
                    event.setCategory(categories.get(event.getCategoryId()));
                }
            }
        }

        return events;
    }

    @Transactional
    public Event put(Event event, int userId, int id, String stateAction) {

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        Optional<Event> foundEvent = repository.findById(id);
        if (foundEvent.isPresent()) {
            Event eventPrevious = foundEvent.get();

            checkEventState(eventPrevious);

            if (event.getEventDate() != null) {
                checkEventDate(event);
            } else {
                event.setEventDate(eventPrevious.getEventDate());
            }

            Optional<Category> category;
            if (event.getCategoryId() != null) {
                category = categoryRepository.findById(event.getCategoryId());
            } else {
                category = categoryRepository.findById(eventPrevious.getCategoryId());
            }
            if (category.isPresent()) {
                event.setCategory(category.get());
                event.setCategoryId(category.get().getId());
            }

            Optional<User> initiator;
            if (event.getInitiatorId() != null) {
                initiator = userRepository.findById(event.getInitiatorId());
            } else {
                initiator = userRepository.findById(eventPrevious.getInitiatorId());
            }
            if (initiator.isPresent()) {
                event.setInitiator(initiator.get());
                event.setInitiatorId(initiator.get().getId());
            } else {
                throw new NotFoundException(String.format("User with id=%d was not found", userId),
                        "The required object was not found.");
            }

            event.setDescription(event.getDescription() == null ? eventPrevious.getDescription() : event.getDescription());
            event.setAnnotation(event.getAnnotation() == null ? eventPrevious.getAnnotation() : event.getAnnotation());
            event.setTitle(event.getTitle() == null ? eventPrevious.getTitle() : event.getTitle());
            event.setIsPaid(event.getIsPaid() == null ? eventPrevious.getIsPaid() : event.getIsPaid());
            event.setRequestModeration(event.getRequestModeration() == null
                    ? eventPrevious.getRequestModeration() : event.getRequestModeration());
            event.setParticipantLimit(event.getParticipantLimit() == null
                    ? eventPrevious.getParticipantLimit() : event.getParticipantLimit());
            event.setLocation(event.getLocation() == null ? eventPrevious.getLocation() : event.getLocation());

            changeEventState(event, stateAction);

            return repository.save(event);

        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", id),
                    "The required object was not found.");
        }
    }

    private void checkEventDate(Event event) {
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException(
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s",
                            event.getEventDate().toString()),
                    "For the requested operation the conditions are not met."
            );
        }
    }

    private void checkEventState(Event event) {
        if (!event.getState().equals(EventState.CANCELED)
                && !event.getState().equals(EventState.PENDING)) {
            throw new EventStatusException("Only pending or canceled events can be changed",
                    "For the requested operation the conditions are not met.");
        }
    }

    private void changeEventState(Event event, String stateAction) {
        StateAction foundStateAction = getRequestedState(stateAction);
        switch (foundStateAction) {
            case SEND_TO_REVIEW:
                event.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                event.setState(EventState.CANCELED);
                break;
            default:
                throw new UnsupportedStateException("Unknown state: " + stateAction, "Incorrectly made request.");
        }
    }

    private StateAction getRequestedState(String requestedStateAction) {
        try {
            StateAction.valueOf(requestedStateAction);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStateException("Unknown state: " + requestedStateAction, "Incorrectly made request.");
        }

        return StateAction.valueOf(requestedStateAction);
    }
}
