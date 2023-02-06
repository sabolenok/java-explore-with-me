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
import ru.practicum.explore_with_me.user.User;
import ru.practicum.explore_with_me.user.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        List<Event> events = eventRepository.findByIdIn(compilationDto.getEvents());
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
        List<EventShortDto> eventsInCompilation = events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
        compilation.setEvents(eventsInCompilation);

        return repository.save(compilation);
    }
}
