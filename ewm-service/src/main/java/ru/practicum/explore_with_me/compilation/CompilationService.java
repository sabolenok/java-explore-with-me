package ru.practicum.explore_with_me.compilation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.compilation.dto.NewCompilationDto;
import ru.practicum.explore_with_me.event.Event;
import ru.practicum.explore_with_me.event.EventMapper;
import ru.practicum.explore_with_me.event.EventRepository;
import ru.practicum.explore_with_me.event.dto.EventShortDto;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.event_category.CategoryRepository;
import ru.practicum.explore_with_me.event_request.EventRequest;
import ru.practicum.explore_with_me.event_request.EventRequestState;
import ru.practicum.explore_with_me.event_request.RequestRepository;
import ru.practicum.explore_with_me.exception.IncorrectCompilationBodyException;
import ru.practicum.explore_with_me.exception.NotFoundException;
import ru.practicum.explore_with_me.user.User;
import ru.practicum.explore_with_me.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CompilationService {

    @Getter
    private final CompilationRepository repository;

    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final RequestRepository requestRepository;

    @Transactional
    public Compilation create(NewCompilationDto compilationDto) {
        if (compilationDto.getTitle() == null) {
            throw new IncorrectCompilationBodyException("Field: title. Error: must not be blank. Value: null",
                    "Incorrectly made request.");
        }

        Compilation compilation = new Compilation();

        compilation.setPinned(compilationDto.isPinned());
        compilation.setTitle(compilationDto.getTitle());
        compilation.setEventsIds(compilationDto.getEvents());

        if (!compilationDto.getEvents().isEmpty()) {
            List<EventShortDto> eventsInCompilation = fillEvents(compilationDto.getEvents())
                    .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
            compilation.setEvents(eventsInCompilation);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        return repository.save(compilation);
    }

    @Transactional
    public Compilation getById(int compId) {
        Optional<Compilation> foundCompilation = repository.findById(compId);
        if (foundCompilation.isEmpty()) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compId),
                    "The required object was not found.");
        }

        Compilation compilation = foundCompilation.get();

        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            List<EventShortDto> eventsInCompilation = fillEvents(
                    compilation.getEvents().stream().map(EventShortDto::getId).collect(Collectors.toSet())
            ).stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
            compilation.setEvents(eventsInCompilation);
        } else {
            compilation.setEventsIds(new HashSet<>());
            compilation.setEvents(new ArrayList<>());
        }

        return compilation;
    }

    @Transactional
    public Compilation update(int compId, NewCompilationDto compilationDto) {
        Optional<Compilation> foundCompilation = repository.findById(compId);
        if (foundCompilation.isEmpty()) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compId),
                    "The required object was not found.");
        }
        Compilation compilation = foundCompilation.get();

        compilation.setTitle(compilationDto.getTitle() == null ? compilation.getTitle() : compilationDto.getTitle());
        compilation.setPinned(compilationDto.isPinned());

        if (compilation.getTitle() == null) {
            throw new IncorrectCompilationBodyException("Field: title. Error: must not be blank. Value: null",
                    "Incorrectly made request.");
        }

        compilation.setEventsIds(new HashSet<>());

        if (!compilationDto.getEvents().isEmpty()) {
            List<EventShortDto> eventsInCompilation = fillEvents(compilationDto.getEvents())
                    .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
            compilation.setEvents(eventsInCompilation);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        return repository.save(compilation);
    }

    private List<Event> fillEvents(Set<Integer> eventIds) {
        List<Event> events = eventRepository.findByIdIn(eventIds);
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
            }
        }
        return events;
    }

    @Transactional
    public void delete(Integer id) {
        if (repository.findById(id).isPresent()) {
            repository.deleteById(id);
        } else {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", id),
                    "The required object was not found.");
        }
    }
}
