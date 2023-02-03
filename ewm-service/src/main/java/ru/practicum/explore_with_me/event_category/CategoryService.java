package ru.practicum.explore_with_me.event_category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.event.Event;
import ru.practicum.explore_with_me.event.EventRepository;
import ru.practicum.explore_with_me.exception.CategoryNotEmptyException;
import ru.practicum.explore_with_me.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    @Getter
    private final CategoryRepository repository;

    private final EventRepository eventRepository;

    @Transactional
    public Category create(Category category) {
        return repository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<Category> getAll(int from, int size) {
        return repository.findAllByOrderById(PageRequest.of(from / size, size));
    }

    @Transactional(readOnly = true)
    public Category getById(Integer id) {
        Optional<Category> category = repository.findById(id);
        if (category.isPresent()) {
            return category.get();
        } else {
            throw new NotFoundException(String.format("Category with id=%d was not found", id),
                    "The required object was not found.");
        }
    }

    @Transactional
    public Category put(int id, Category category) {
        category.setId(id);
        Optional<Category> previous = repository.findById(id);
        if (previous.isPresent()) {
            category.setName(
                    (category.getName() == null || category.getName().isBlank())
                            ? previous.get().getName()
                            : category.getName()
            );
        } else {
            throw new NotFoundException(String.format("Category with id=%d was not found", id),
                    "The required object was not found.");
        }
        return repository.save(category);
    }

    @Transactional
    public void delete(Integer id) {
        if (repository.findById(id).isPresent()) {
            if (isCategoryEmpty(id)) {
                repository.deleteById(id);
            } else {
                throw new CategoryNotEmptyException("The category is not empty",
                        "For the requested operation the conditions are not met.");
            }
        } else {
            throw new NotFoundException(String.format("Category with id=%d was not found", id),
                    "The required object was not found.");
        }
    }

    private boolean isCategoryEmpty(int id) {
        List<Event> categoryEvents = eventRepository.findByCategoryId(id);
        return categoryEvents.isEmpty();
    }
}
