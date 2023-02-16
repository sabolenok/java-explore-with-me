package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import ru.practicum.explore_with_me.event.dto.EventState;
import ru.practicum.explore_with_me.event_category.Category;
import ru.practicum.explore_with_me.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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
    private Boolean isPaid;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Column(name = "participant_limit")
    private Integer participantLimit;
    private Integer views;
    @Transient
    private Integer confirmedRequests;
    @Column(name = "initiator_id")
    private Integer initiatorId;
    @Transient
    @ManyToOne(fetch = FetchType.LAZY)
    private User initiator;
    @Column(name = "category_id")
    private Integer categoryId;
    @Transient
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    @Embedded
    private Location location;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "comments", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "id")
    private List<Integer> commentsIds;
    @Transient
    private List<Comment> comments;
}
