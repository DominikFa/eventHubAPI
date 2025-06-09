package com.example.eventhubapi.event.mapper;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.location.Location;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.user.User;
import org.springframework.stereotype.Service;

@Service
public class EventMapper {

    public EventDto toDto(Event event) {
        if (event == null) return null;

        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setDescription(event.getDescription());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setPublic(event.isPublic());
        dto.setMaxParticipants(event.getMaxParticipants());

        if (event.getOrganizer() != null) {
            User organizer = event.getOrganizer();
            dto.setOrganizer(new UserSummary(organizer.getId(), organizer.getName(), organizer.getProfileImageUrl()));
        }

        // Location and participant count would be set here
        // dto.setParticipantsCount(event.getParticipants().size());
        // dto.setLocation(toLocationDto(event.getLocation()));
        return dto;
    }

    public Event toEntity(EventCreationRequest request, User organizer) {
        if (request == null) return null;

        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setPublic(request.getIsPublic());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setOrganizer(organizer);

        if (request.getLocationId() != null) {
            // In a real scenario, you'd fetch the Location entity from the database
            // For now, we'll just create a placeholder
            Location location = new Location();
            location.setId(request.getLocationId());
            event.setLocation(location);
        }
        return event;
    }
}