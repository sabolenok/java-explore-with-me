package ru.practicum.explore_with_me.event;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.event.dto.EventFullDto;
import ru.practicum.explore_with_me.event.dto.EventShortDto;
import ru.practicum.explore_with_me.event.dto.NewEventDto;
import ru.practicum.explore_with_me.event.dto.UpdateEventUserRequestDto;
import ru.practicum.explore_with_me.user.UserMapper;

@Component
public class EventMapper {

    public static NewEventDto toEventDto(Event event) {
        NewEventDto newEventDto = new NewEventDto();
        newEventDto.setId(event.getId());
        newEventDto.setAnnotation(event.getAnnotation());
        newEventDto.setDescription(event.getDescription());
        newEventDto.setPaid(event.getIsPaid());
        newEventDto.setEventDate(event.getEventDate());
        newEventDto.setTitle(event.getTitle());
        newEventDto.setRequestModeration(event.getRequestModeration());
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
        event.setIsPaid(eventDto.isPaid());
        event.setEventDate(eventDto.getEventDate());
        event.setTitle(eventDto.getTitle());
        event.setRequestModeration(eventDto.isRequestModeration());
        event.setParticipantLimit(eventDto.getParticipantLimit());
        event.setLocation(eventDto.getLocation());
        event.setInitiatorId(eventDto.getInitiator());
        event.setCategoryId(eventDto.getCategory());
        return event;
    }

    public static Event toEvent(UpdateEventUserRequestDto eventDto) {
        Event event = new Event();
        event.setId(eventDto.getId());
        event.setAnnotation(eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription());
        event.setIsPaid(eventDto.getPaid());
        event.setEventDate(eventDto.getEventDate());
        event.setTitle(eventDto.getTitle());
        event.setRequestModeration(eventDto.getRequestModeration());
        event.setParticipantLimit(eventDto.getParticipantLimit());
        event.setLocation(eventDto.getLocation());
        event.setInitiatorId(eventDto.getInitiator());
        event.setCategoryId(eventDto.getCategory());
        return event;
    }

    public static EventFullDto toEventFullDto(Event event) {
        EventFullDto newEventDto = new EventFullDto();
        newEventDto.setId(event.getId());
        newEventDto.setAnnotation(event.getAnnotation());
        newEventDto.setDescription(event.getDescription());
        newEventDto.setPaid(event.getIsPaid());
        newEventDto.setEventDate(event.getEventDate());
        newEventDto.setTitle(event.getTitle());
        newEventDto.setRequestModeration(event.getRequestModeration());
        newEventDto.setParticipantLimit(event.getParticipantLimit());
        newEventDto.setLocation(event.getLocation());
        newEventDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        newEventDto.setCategory(event.getCategory());
        newEventDto.setCreatedOn(event.getCreatedOn());
        newEventDto.setState(event.getState());
        return newEventDto;
    }

    public static EventShortDto toEventShortDto(Event event) {
        EventShortDto newEventDto = new EventShortDto();
        newEventDto.setId(event.getId());
        newEventDto.setAnnotation(event.getAnnotation());
        newEventDto.setPaid(event.getIsPaid());
        newEventDto.setEventDate(event.getEventDate());
        newEventDto.setTitle(event.getTitle());
        newEventDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        newEventDto.setCategory(event.getCategory());
        return newEventDto;
    }
}
