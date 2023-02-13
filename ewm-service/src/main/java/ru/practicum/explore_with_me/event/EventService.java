package ru.practicum.explore_with_me.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.client.StatsClient;
import ru.practicum.explore_with_me.dto.Stat;
import ru.practicum.explore_with_me.dto.StatWithCount;
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

import javax.servlet.http.HttpServletRequest;
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

    private final StatsClient statsClient;

    @Value("${stats-server.url}")
    private String serverUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int DAYS_FOR_STATS_START_DATE = 180;

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
            return fillInformation(List.of(event), null).get(0);
        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId),
                    "The required object was not found.");
        }
    }

    @Transactional(readOnly = true)
    public Event getPublished(int eventId, HttpServletRequest request) throws JsonProcessingException {

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
            fillInformation(List.of(event), null);
            fillStat(List.of(event), request);
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

        List<Event> foundEvents = fillInformation(events.getContent(), null);

        return new PageImpl<>(foundEvents, PageRequest.of(from, size), foundEvents.size());
    }

    @Transactional(readOnly = true)
    public Page<Event> getAllForAdminWithFilters(Integer[] users, String[] states, Integer[] categories,
                                                 String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime startDate = (rangeStart == null || rangeStart.isBlank()) ? null : LocalDateTime.parse(rangeStart, FORMATTER);
        LocalDateTime endDate = (rangeEnd == null || rangeEnd.isBlank()) ? null : LocalDateTime.parse(rangeEnd, FORMATTER);
        Page<Event> events =  repository.findAll(
                where(hasInitiatorIn(users))
                        .and(hasStatesIn(states))
                        .and(hasCategoriesIn(categories))
                        .and(hasStartAfter(startDate))
                        .and(hasEndBefore(endDate)),
                PageRequest.of(from / size, size)
        );

        List<Event> foundEvents = fillInformation(events.getContent(), null);

        return new PageImpl<>(foundEvents, PageRequest.of(from, size), foundEvents.size());
    }

    @Transactional(readOnly = true)
    public Page<Event> getAllForPublicWithFilters(String text, Integer[] categories, Boolean paid,
                                                  String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort,
                                                 Integer from, Integer size,
                                                  HttpServletRequest request) throws JsonProcessingException {
        String sortProperty = "eventDate";
        EventSort eventSort = null;
        if (sort != null && !sort.isBlank()) {
            try {
                eventSort = EventSort.valueOf(sort);
            } catch (IllegalArgumentException ignored) {
            }
            sortProperty = EventSort.VIEWS.equals(eventSort) ? "views" : "eventDate";
        }
        LocalDateTime startDate = (rangeStart == null || rangeStart.isBlank()) ? null : LocalDateTime.parse(rangeStart, FORMATTER);
        LocalDateTime endDate = (rangeEnd == null || rangeEnd.isBlank()) ? null : LocalDateTime.parse(rangeEnd, FORMATTER);

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

        List<Event> foundEvents = fillInformation(events.getContent(), onlyAvailable);
        fillStat(foundEvents, request);

        return new PageImpl<>(foundEvents, PageRequest.of(from, size), foundEvents.size());
    }

    public List<Event> fillInformation(List<Event> events, Boolean onlyAvailable) {
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
            List<EventRequest> requests = requestRepository.findByEventIdInAndStatus(
                    events.stream().map(Event::getId).collect(Collectors.toList()),
                    EventRequestState.CONFIRMED
            );
            for (Event event : events) {
                if (foundCategories.containsKey(event.getCategoryId())) {
                    event.setCategory(foundCategories.get(event.getCategoryId()));
                }
                if (foundInitiators.containsKey(event.getInitiatorId())) {
                    event.setInitiator(foundInitiators.get(event.getInitiatorId()));
                }
                long confirmedRequests = requests.stream().filter(r -> r.getEventId() == event.getId()).count();
                event.setConfirmedRequests((int) confirmedRequests);
                if (onlyAvailable != null && onlyAvailable) {
                    if (event.getParticipantLimit() < confirmedRequests) {
                        events.remove(event);
                    }
                }
            }
        }
        return events;
    }

    private Specification<Event> hasInitiatorIn(Integer[] userIds) {
        return (event, query, cb) -> (userIds != null && userIds.length > 0)
                ? event.get("initiatorId").in(userIds) : null;
    }

    private Specification<Event> hasStatesIn(String[] states) {
        if (states == null) {
            return null;
        }
        Object[] st = Arrays.stream(states).map(EventState::valueOf).toArray();
        return (event, query, cb) -> states.length > 0 ? event.get("state").in(st) : null;
    }

    private Specification<Event> hasCategoriesIn(Integer[] categories) {
        return (event, query, cb) -> (categories != null && categories.length > 0)
                ? event.get("categoryId").in(categories) : null;
    }

    private Specification<Event> hasStartAfter(LocalDateTime rangeStart) {
        return (event, query, cb) -> rangeStart == null ? null : cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart);
    }

    private Specification<Event> hasEndBefore(LocalDateTime rangeEnd) {
        return (event, query, cb) -> rangeEnd == null ? null : cb.lessThanOrEqualTo(event.get("eventDate"), rangeEnd);
    }

    private Specification<Event> hasStartAfterNow(LocalDateTime rangeStart) {
        return (event, query, cb) -> rangeStart == null
                ? cb.greaterThanOrEqualTo(event.get("eventDate"), LocalDateTime.now())
                : cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart);
    }

    private Specification<Event> hasAnnotationEqualText(String text) {
        return (event, query, cb) -> (text == null || text.isBlank())
                ? null
                : cb.or(cb.like(cb.lower(event.get("annotation")), "%" + text.toLowerCase() + "%"),
                cb.like(cb.lower(event.get("description")), "%" + text.toLowerCase() + "%"));
    }

    private Specification<Event> isPaid(Boolean paid) {
        return (event, query, cb) -> paid == null ? null : cb.equal(event.get("isPaid"), paid);
    }

    private Specification<Event> hasAvailable(Boolean onlyAvailable) {
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

            fillInformationInUpdatedEvent(event, eventPrevious, stateAction, false);

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

            fillInformationInUpdatedEvent(event, eventPrevious, stateAction, true);

            return repository.save(event);

        } else {
            throw new NotFoundException(String.format("Event with id=%d was not found", id),
                    "The required object was not found.");
        }
    }

    private void fillInformationInUpdatedEvent(Event event, Event eventPrevious, String stateAction, boolean isAdmin) {
        fillDatesInUpdatedEvent(event, eventPrevious, isAdmin);

        fillCategoryInUpdatedEvent(event, eventPrevious);

        fillInitiatorInUpdatedEvent(event, eventPrevious);

        fillConfirmedRequestsInEvent(event);

        fillStateInUpdatedEvent(event, eventPrevious, stateAction);

        event.setDescription(event.getDescription() == null ? eventPrevious.getDescription() : event.getDescription());
        event.setAnnotation(event.getAnnotation() == null ? eventPrevious.getAnnotation() : event.getAnnotation());
        event.setTitle(event.getTitle() == null ? eventPrevious.getTitle() : event.getTitle());
        event.setIsPaid(event.getIsPaid() == null ? eventPrevious.getIsPaid() : event.getIsPaid());
        event.setRequestModeration(event.getRequestModeration() == null
                ? eventPrevious.getRequestModeration() : event.getRequestModeration());
        event.setParticipantLimit(event.getParticipantLimit() == null
                ? eventPrevious.getParticipantLimit() : event.getParticipantLimit());
        event.setLocation(event.getLocation() == null ? eventPrevious.getLocation() : event.getLocation());
    }

    private void fillDatesInUpdatedEvent(Event event, Event eventPrevious, boolean isAdmin) {
        event.setCreatedOn(event.getCreatedOn() == null ? eventPrevious.getCreatedOn() : event.getCreatedOn());
        if (event.getEventDate() != null) {
            checkEventDate(event, isAdmin);
        } else {
            event.setEventDate(eventPrevious.getEventDate());
        }
    }

    private void fillCategoryInUpdatedEvent(Event event, Event eventPrevious) {
        Integer categoryId = event.getCategoryId() != null ? event.getCategoryId() : eventPrevious.getCategoryId();
        Optional<Category> category = categoryRepository.findById(categoryId);

        if (category.isPresent()) {
            event.setCategory(category.get());
            event.setCategoryId(categoryId);
        }
    }

    private void fillInitiatorInUpdatedEvent(Event event, Event eventPrevious) {
        Integer userId = event.getInitiatorId() != null ? event.getInitiatorId() : eventPrevious.getInitiatorId();
        Optional<User> initiator = userRepository.findById(userId);

        if (initiator.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId),
                    "The required object was not found.");
        }

        event.setInitiator(initiator.get());
        event.setInitiatorId(userId);
    }

    private void fillConfirmedRequestsInEvent(Event event) {
        int confirmedRequests = requestRepository.findByEventIdAndStatus(
                event.getId(), EventRequestState.CONFIRMED
                ).size();
        event.setConfirmedRequests(confirmedRequests);
    }

    private void fillStateInUpdatedEvent(Event event, Event eventPrevious, String stateAction) {
        EventState currentState = event.getState() == null ? eventPrevious.getState() : event.getState();
        changeEventState(event, stateAction, currentState);
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
                publishEvent(event, currentState);
                break;
            case REJECT_EVENT:
                rejectEvent(event, currentState);
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

    private void publishEvent(Event event, EventState currentState) {
        if (currentState.equals(EventState.PENDING)) {
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else {
            throw new EventStatusAdminException(
                    String.format("Cannot publish the event because it's not in the right state: %s", currentState),
                    "For the requested operation the conditions are not met."
            );
        }
    }

    private void rejectEvent(Event event, EventState currentState) {
        if (!currentState.equals(EventState.PUBLISHED)) {
            event.setState(EventState.CANCELED);
        } else {
            throw new EventStatusAdminException(
                    String.format("Cannot publish the event because it's not in the right state: %s", currentState),
                    "For the requested operation the conditions are not met."
            );
        }
    }

    private void fillStat(List<Event> events, HttpServletRequest request) throws JsonProcessingException {
        MutableHttpRequest wrappedRequest = new MutableHttpRequest(request);
        wrappedRequest.addParameter("start", LocalDateTime.now().minusDays(DAYS_FOR_STATS_START_DATE).format(FORMATTER));
        wrappedRequest.addParameter("end", LocalDateTime.now().format(FORMATTER));

        statsClient.setServerUrl(serverUrl);
        List<StatWithCount> statList = statsClient.getStats(wrappedRequest);
        int hits = (int) statList.stream().filter(s -> Objects.equals(s.getUri(), request.getRequestURI())).count();
        for (Event event : events) {
            event.setViews(hits + 1);
        }
        Stat stat = new Stat();
        stat.setIp(request.getRemoteAddr());
        stat.setUri(request.getRequestURI());
        stat.setTimestamp(LocalDateTime.now());
        stat.setApp(request.getHeader("User-Agent"));
        statsClient.hit(stat);

    }
}
