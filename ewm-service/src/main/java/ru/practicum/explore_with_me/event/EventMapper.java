package ru.practicum.explore_with_me.event;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.event.dto.EventShortDto;
import ru.practicum.explore_with_me.event.dto.NewEventDto;

@Component
public class EventMapper {

    public static NewEventDto toEventDto(Event event) {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setId(event.getId());
        newEventDto.setAnnotation(event.getAnnotation());
        newEventDto.setDescription(event.getDescription());
        newEventDto.setPaid(event.isPaid());
        newEventDto.setEventDate(event.getEventDate());
        newEventDto.setTitle(event.getTitle());
        newEventDto.setRequestModeration(event.isRequestModeration());
        newEventDto.setParticipantLimit(event.getParticipantLimit());
        newEventDto.setLocation(event.getLocation());
        newEventDto.setInitiator(event.getInitiatorId());
        newEventDto.setCategory(event.getCategoryId());
        return newEventDto;
    }

    public static Event toEvent(NewEventDto eventDto) {
        Event event = new Event();
        event.setId(eventDto.getId());
        event.setAnnotation(eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription());
        event.setPaid(eventDto.isPaid());
        event.setEventDate(eventDto.getEventDate());
        event.setTitle(eventDto.getTitle());
        event.setRequestModeration(eventDto.isRequestModeration());
        event.setParticipantLimit(eventDto.getParticipantLimit());
        event.setLocation(eventDto.getLocation());
        event.setInitiatorId(eventDto.getInitiator());
        event.setCategoryId(eventDto.getCategory());
        return event;
    }

    public static EventShortDto toEventShortDto(Event event) {
        EventShortDto newEventDto = new EventShortDto();
        newEventDto.setId(event.getId());
        newEventDto.setAnnotation(event.getAnnotation());
        newEventDto.setDescription(event.getDescription());
        newEventDto.setPaid(event.isPaid());
        newEventDto.setEventDate(event.getEventDate());
        newEventDto.setTitle(event.getTitle());
        newEventDto.setRequestModeration(event.isRequestModeration());
        newEventDto.setParticipantLimit(event.getParticipantLimit());
        newEventDto.setLocation(event.getLocation());
        newEventDto.setInitiator(event.getInitiatorId());
        newEventDto.setCategory(event.getCategoryId());
        newEventDto.setCreatedOn(event.getCreatedOn());
        newEventDto.setState(event.getState());
        return newEventDto;
    }
}
