package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@DynamicUpdate
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String annotation;
    private String description;
    @Column(nullable = false)
    private String title;
    @Enumerated(EnumType.STRING)
    private EventState state;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;
    @Column(name = "request_moderation")
    private boolean requestModeration;
    @Column(name = "participant_limit")
    private int participantLimit;
    private int views;
    private int confirmedRequests;
    @Column(name = "initiator_id")
    private int initiatorId;
    @Transient
    private User initiator;
    @Column(name = "category_id")
    private int categoryId;
    @Transient
    private Category category;
}
