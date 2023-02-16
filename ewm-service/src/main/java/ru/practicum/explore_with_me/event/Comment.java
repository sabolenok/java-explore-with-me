package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotBlank(message = "Text shouldn't be empty")
    @Column(name = "text")
    private String text;
    @Column(name = "event_id")
    private Integer eventId;
    @Column(name = "author_id")
    private Integer authorId;
    private LocalDateTime created;

}
