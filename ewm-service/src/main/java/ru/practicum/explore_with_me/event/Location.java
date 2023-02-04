package ru.practicum.explore_with_me.event;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Embeddable
@AttributeOverrides({
        @AttributeOverride( name = "lat", column = @Column(name = "location_lat")),
        @AttributeOverride( name = "lon", column = @Column(name = "location_lon"))
})
@Getter
@Setter
public class Location {
    private float lat;
    private float lon;
}
