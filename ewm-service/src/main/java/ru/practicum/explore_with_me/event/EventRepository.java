package ru.practicum.explore_with_me.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {
    List<Event> findByCategoryId(int categoryId);

    Page<Event> findByInitiatorId(int initiatorId, Pageable pageable);

    List<Event> findByIdIn(Collection<Integer> id);
}
