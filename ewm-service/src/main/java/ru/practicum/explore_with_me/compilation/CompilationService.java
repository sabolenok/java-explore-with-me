package ru.practicum.explore_with_me.compilation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.compilation.dto.NewCompilationDto;
import ru.practicum.explore_with_me.event.Event;
import ru.practicum.explore_with_me.event.EventMapper;
import ru.practicum.explore_with_me.event.EventRepository;
import ru.practicum.explore_with_me.event.EventService;
import ru.practicum.explore_with_me.event.dto.EventShortDto;
import ru.practicum.explore_with_me.exception.IncorrectCompilationBodyException;
import ru.practicum.explore_with_me.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CompilationService {

    @Getter
    private final CompilationRepository repository;

    private final EventRepository eventRepository;

    private final EventService eventService;

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

        if (compilation.getEventsIds() != null && !compilation.getEventsIds().isEmpty()) {
            List<EventShortDto> eventsInCompilation = fillEvents(compilation.getEventsIds())
                    .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
            compilation.setEvents(eventsInCompilation);
        } else {
            compilation.setEventsIds(new HashSet<>());
            compilation.setEvents(new ArrayList<>());
        }

        return compilation;
    }

    @Transactional
    public List<Compilation> getAll(Boolean pinned, Integer from, Integer size) {

        Page<Compilation> compilations =  repository.findAll(
                where(isPinned(pinned)),
                PageRequest.of(from / size, size)
        );

        List<Compilation> foundCompilations = compilations.getContent();

        for (Compilation compilation : foundCompilations) {
            if (compilation.getEventsIds() != null && !compilation.getEventsIds().isEmpty()) {
                List<EventShortDto> eventsInCompilation = fillEvents(compilation.getEventsIds())
                        .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
                compilation.setEvents(eventsInCompilation);
            } else {
                compilation.setEventsIds(new HashSet<>());
                compilation.setEvents(new ArrayList<>());
            }
        }

        return foundCompilations;
    }

    private Specification<Compilation> isPinned(Boolean pinned) {
        return (compilation, query, cb) -> pinned == null ? null : cb.equal(compilation.get("isPinned"), pinned);
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
            compilation.setEventsIds(compilationDto.getEvents());
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        return repository.save(compilation);
    }

    private List<Event> fillEvents(Set<Integer> eventIds) {
        return eventService.fillInformation(eventRepository.findByIdIn(eventIds), null);
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
