package ru.practicum.explore_with_me.event_request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import ru.practicum.explore_with_me.event.Event;
import ru.practicum.explore_with_me.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@DynamicUpdate
@Getter
@Setter
public class EventRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDateTime created;
    @Column(name = "event_id")
    private int eventId;
    @Transient
    private Event event;
    @Column(name = "requester_id")
    private int requesterId;
    @Transient
    @ManyToOne(fetch = FetchType.LAZY)
    private User requester;
    @Enumerated(EnumType.STRING)
    private EventRequestState status;
}
