package ru.practicum.explore_with_me.compilation;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import ru.practicum.explore_with_me.event.dto.EventDto;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "compilations")
@DynamicUpdate
@Getter
@Setter
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;
    @Column(nullable = false)
    private String title;
    @Transient
    List<EventDto> events;
}
