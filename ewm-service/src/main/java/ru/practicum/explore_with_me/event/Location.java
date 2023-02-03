package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "locations")
@DynamicUpdate
@Getter
@Setter
public class Location {
    @Id
    @Column(name = "event_id")
    private int eventId;
    private float lat;
    private float lon;
}
