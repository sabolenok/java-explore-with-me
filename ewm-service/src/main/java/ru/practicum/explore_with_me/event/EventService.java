package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.event.dto.EventSort;
import ru.practicum.explore_with_me.event.dto.EventState;
import ru.practicum.explore_with_me.event.dto.StateAction;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.event_category.CategoryRepository;
import ru.practicum.explore_with_me.event_request.EventRequest;
import ru.practicum.explore_with_me.event_request.EventRequestState;
import ru.practicum.explore_with_me.event_request.RequestRepository;
import ru.practicum.explore_with_me.exception.*;
import ru.practicum.explore_with_me.user.User;
import ru.practicum.explore_with_me.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EventService {

    @Getter
    private final EventRepository repository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final RequestRepository requestRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public Event create(Event event, Integer userId) {
        checkEventDate(event, false);

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
    public Event getPublished(int eventId) {

        Optional<Event> foundEvent = repository.findById(eventId);
        if (foundEvent.isPresent()) {
            Event event = foundEvent.get();
            if (!event.getState().equals(EventState.PUBLISHED)) {
                throw new NotFoundException(String.format("Event with id=%d was not found", eventId),
                        "The required object was not found.");
            }

            Optional<User> foundUser = userRepository.findById(event.getInitiatorId());
            if (foundUser.isEmpty()) {
                throw new NotFoundException(String.format("User with id=%d was not found", event.getInitiatorId()),
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

    @Transactional(readOnly = true)
    public Page<Event> getAllForAdminWithFilters(Integer[] users, String[] states, Integer[] categories,
                                                 String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime startDate = (rangeStart == null || rangeStart.isBlank()) ? null : LocalDateTime.parse(rangeStart, formatter);
        LocalDateTime endDate = (rangeEnd == null || rangeEnd.isBlank()) ? null : LocalDateTime.parse(rangeEnd, formatter);
        Page<Event> events =  repository.findAll(
                where(hasInitiatorIn(users))
                        .and(hasStatesIn(states))
                        .and(hasCategoriesIn(categories))
                        .and(hasStartAfter(startDate))
                        .and(hasEndBefore(endDate)),
                PageRequest.of(from / size, size)
        );

        Set<Integer> eventsCategories = new HashSet<>();
        Set<Integer> eventsInitiators = new HashSet<>();
        for (Event event : events) {
            eventsCategories.add(event.getCategoryId());
            eventsInitiators.add(event.getInitiatorId());
        }

        if (!eventsCategories.isEmpty()) {
            Map<Integer, Category> foundCategories = categoryRepository.findAllById(eventsCategories)
                    .stream().collect(Collectors.toMap(Category::getId, category -> category));
            Map<Integer, User> foundInitiators = userRepository.findAllById(eventsInitiators)
                    .stream().collect(Collectors.toMap(User::getId, user -> user));
            for (Event event : events) {
                if (foundCategories.containsKey(event.getCategoryId())) {
                    event.setCategory(foundCategories.get(event.getCategoryId()));
                }
                if (foundInitiators.containsKey(event.getInitiatorId())) {
                    event.setInitiator(foundInitiators.get(event.getInitiatorId()));
                }
            }
        }

        return events;
    }

    @Transactional(readOnly = true)
    public Page<Event> getAllForPublicWithFilters(String text, Integer[] categories, Boolean paid,
                                                  String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort,
                                                 Integer from, Integer size) {
        String sortProperty = "eventDate";
        EventSort eventSort = null;
        if (sort != null && !sort.isBlank()) {
            try {
                eventSort = EventSort.valueOf(sort);
            } catch(IllegalArgumentException ignored) {
            }
            sortProperty = EventSort.VIEWS.equals(eventSort) ? "views" : "eventDate";
        }
        LocalDateTime startDate = (rangeStart == null || rangeStart.isBlank()) ? null : LocalDateTime.parse(rangeStart, formatter);
        LocalDateTime endDate = (rangeEnd == null || rangeEnd.isBlank()) ? null : LocalDateTime.parse(rangeEnd, formatter);

        String[] states = { EventState.PENDING.toString() };

        Page<Event> events =  repository.findAll(
                where(hasAnnotationEqualText(text))
                        .and(hasCategoriesIn(categories))
                        .and(isPaid(paid))
                        .and(hasStartAfterNow(startDate))
                        .and(hasEndBefore(endDate))
                        .and(hasAvailable(onlyAvailable))
                        .and(hasStatesIn(states)),
                PageRequest.of(from / size, size, Sort.by(sortProperty))
        );

        List<Event> foundEvents = events.getContent();
        Set<Integer> eventsCategories = new HashSet<>();
        Set<Integer> eventsInitiators = new HashSet<>();
        for (Event event : foundEvents) {
            eventsCategories.add(event.getCategoryId());
            eventsInitiators.add(event.getInitiatorId());
        }

        if (!eventsCategories.isEmpty()) {
            Map<Integer, Category> foundCategories = categoryRepository.findAllById(eventsCategories)
                    .stream().collect(Collectors.toMap(Category::getId, category -> category));
            Map<Integer, User> foundInitiators = userRepository.findAllById(eventsInitiators)
                    .stream().collect(Collectors.toMap(User::getId, user -> user));
            List<EventRequest> requests = requestRepository.findByEventIdInAndStatus(
                    foundEvents.stream().map(Event::getId).collect(Collectors.toList()),
                    EventRequestState.CONFIRMED
            );
            for (Event event : foundEvents) {
                if (foundCategories.containsKey(event.getCategoryId())) {
                    event.setCategory(foundCategories.get(event.getCategoryId()));
                }
                if (foundInitiators.containsKey(event.getInitiatorId())) {
                    event.setInitiator(foundInitiators.get(event.getInitiatorId()));
                }
                if (onlyAvailable != null && onlyAvailable) {
                    long confirmedRequests = requests.stream().filter(r -> r.getEventId() == event.getId()).count();
                    if (event.getParticipantLimit() < confirmedRequests) {
                        foundEvents.remove(event);
                    }
                }
            }
        }

        return new PageImpl<>(foundEvents, PageRequest.of(from, size), foundEvents.size());
    }

    static Specification<Event> hasInitiatorIn(Integer[] userIds) {
        return (event, query, cb) -> (userIds != null && userIds.length > 0)
                ? event.get("initiatorId").in(userIds) : null;
    }

    static Specification<Event> hasStatesIn(String[] states) {
        if (states == null) {
            return null;
        }
        Object[] st = Arrays.stream(states).map(EventState::valueOf).toArray();
        return (event, query, cb) -> states.length > 0 ? event.get("state").in(st) : null;
    }

    static Specification<Event> hasCategoriesIn(Integer[] categories) {
        return (event, query, cb) -> (categories != null && categories.length > 0)
                ? event.get("categoryId").in(categories) : null;
    }

    static Specification<Event> hasStartAfter(LocalDateTime rangeStart) {
        return (event, query, cb) -> rangeStart == null ? null : cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart);
    }

    static Specification<Event> hasEndBefore(LocalDateTime rangeEnd) {
        return (event, query, cb) -> rangeEnd == null ? null : cb.lessThanOrEqualTo(event.get("eventDate"), rangeEnd);
    }

    static Specification<Event> hasStartAfterNow(LocalDateTime rangeStart) {
        return (event, query, cb) -> rangeStart == null
                ? cb.greaterThanOrEqualTo(event.get("eventDate"), LocalDateTime.now())
                : cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart);
    }

    static Specification<Event> hasAnnotationEqualText(String text) {
        return (event, query, cb) -> (text == null || text.isBlank())
                ? null
                : cb.or(cb.like(cb.lower(event.get("annotation")), "%" + text.toLowerCase() + "%"),
                cb.like(cb.lower(event.get("description")), "%" + text.toLowerCase() + "%"));
    }

    static Specification<Event> isPaid(Boolean paid) {
        return (event, query, cb) -> paid == null ? null : cb.equal(event.get("isPaid"), paid);
    }

    static Specification<Event> hasAvailable(Boolean onlyAvailable) {
        return (event, query, cb) -> (onlyAvailable == null || !onlyAvailable)
                ? null
                : cb.greaterThan(event.get("participantLimit"), event.get("confirmedRequests"));
    }

    @Transactional
    public Event putByUser(Event event, int userId, int id, String stateAction) {

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        Optional<Event> foundEvent = repository.findById(id);
        if (foundEvent.isPresent()) {
            Event eventPrevious = foundEvent.get();

            checkEventState(eventPrevious);

            fillEventInformation(event, eventPrevious, stateAction, false);

            return repository.save(event);

        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", id),
                    "The required object was not found.");
        }
    }

    @Transactional
    public Event putByAdmin(Event event, int id, String stateAction) {

        Optional<Event> foundEvent = repository.findById(id);
        if (foundEvent.isPresent()) {
            Event eventPrevious = foundEvent.get();

            fillEventInformation(event, eventPrevious, stateAction, true);

            return repository.save(event);

        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", id),
                    "The required object was not found.");
        }
    }

    private Event fillEventInformation(Event event, Event eventPrevious, String stateAction, boolean isAdmin) {
        event.setCreatedOn(event.getCreatedOn() == null ? eventPrevious.getCreatedOn() : event.getCreatedOn());
        if (event.getEventDate() != null) {
            checkEventDate(event, isAdmin);
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

        Integer userId;
        if (event.getInitiatorId() != null) {
            userId = event.getInitiatorId();
        } else {
            userId = eventPrevious.getInitiatorId();
        }
        Optional<User> initiator = userRepository.findById(userId);
        if (initiator.isPresent()) {
            event.setInitiator(initiator.get());
            event.setInitiatorId(initiator.get().getId());
        } else {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        int confirmedRequests = requestRepository.findByEventIdAndStatus(event.getId(), EventRequestState.CONFIRMED)
                .size();
        event.setConfirmedRequests(confirmedRequests);

        event.setDescription(event.getDescription() == null ? eventPrevious.getDescription() : event.getDescription());
        event.setAnnotation(event.getAnnotation() == null ? eventPrevious.getAnnotation() : event.getAnnotation());
        event.setTitle(event.getTitle() == null ? eventPrevious.getTitle() : event.getTitle());
        event.setIsPaid(event.getIsPaid() == null ? eventPrevious.getIsPaid() : event.getIsPaid());
        event.setRequestModeration(event.getRequestModeration() == null
                ? eventPrevious.getRequestModeration() : event.getRequestModeration());
        event.setParticipantLimit(event.getParticipantLimit() == null
                ? eventPrevious.getParticipantLimit() : event.getParticipantLimit());
        event.setLocation(event.getLocation() == null ? eventPrevious.getLocation() : event.getLocation());

        EventState currentState = event.getState() == null ? eventPrevious.getState() : event.getState();
        changeEventState(event, stateAction, currentState);

        return event;
    }

    private void checkEventDate(Event event, boolean isAdmin) {
        LocalDateTime dateForChecking = isAdmin ? event.getCreatedOn().plusHours(1) : LocalDateTime.now().plusHours(2);
        if (event.getEventDate().isBefore(dateForChecking)) {
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

    private void changeEventState(Event event, String stateAction, EventState currentState) {
        StateAction foundStateAction = getRequestedState(stateAction);
        switch (foundStateAction) {
            case SEND_TO_REVIEW:
                event.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                event.setState(EventState.CANCELED);
                break;
            case PUBLISH_EVENT:
                if (currentState.equals(EventState.PENDING)) {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else {
                    throw new EventStatusAdminException(
                            String.format("Cannot publish the event because it's not in the right state: %s", currentState),
                            "For the requested operation the conditions are not met."
                    );
                }
                break;
            case REJECT_EVENT:
                if (!currentState.equals(EventState.PUBLISHED)) {
                    event.setState(EventState.CANCELED);
                } else {
                    throw new EventStatusAdminException(
                            String.format("Cannot publish the event because it's not in the right state: %s", currentState),
                            "For the requested operation the conditions are not met."
                    );
                }
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
