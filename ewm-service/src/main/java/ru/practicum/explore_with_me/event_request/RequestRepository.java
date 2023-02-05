package ru.practicum.explore_with_me.event_request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestRepository extends JpaRepository<EventRequest, Integer> {

    Optional<EventRequest> findByRequesterIdAndEventId(int requesterId, int eventId);

    Page<EventRequest> findAllByRequesterId(int requesterId, Pageable pageable);
}
