package ru.practicum.explore_with_me.event_request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestRepository extends JpaRepository<EventRequest, Integer> {

    Optional<EventRequest> findByRequesterIdAndEventId(int requesterId, int eventId);
}
