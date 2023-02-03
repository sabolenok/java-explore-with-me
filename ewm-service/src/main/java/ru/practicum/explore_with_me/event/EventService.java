package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.event_category.CategoryRepository;
import ru.practicum.explore_with_me.exception.EventDateException;
import ru.practicum.explore_with_me.user.User;
import ru.practicum.explore_with_me.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

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
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        checkEventDate(event);
        Optional<Category> category = categoryRepository.findById(event.getCategoryId());
        category.ifPresent(event::setCategory);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            event.setInitiator(user.get());
            event.setInitiatorId(userId);

            return repository.save(event);
        }
        return null;
    }

    private void checkEventDate(Event event) {
        if (event.getEventDate().isBefore(event.getCreatedOn().plusHours(2))) {
            throw new EventDateException(
                    String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s",
                            event.getEventDate().toString()),
                    "For the requested operation the conditions are not met."
            );
        }
    }
}
