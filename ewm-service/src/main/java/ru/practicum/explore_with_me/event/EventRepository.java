package ru.practicum.explore_with_me.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByCategoryId(int categoryId);
}
