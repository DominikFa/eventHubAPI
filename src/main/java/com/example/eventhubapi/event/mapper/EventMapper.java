package com.example.eventhubapi.event.mapper;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.user.User;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.example.eventhubapi.location.mapper.LocationMapper;


@Service
public class EventMapper {

    private final LocationMapper locationMapper;

    public EventMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }
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
            String organizerName = organizer.getProfile() != null ? organizer.getProfile().getName() : null;
            String imageUrl = null;
            if (organizer.getProfile() != null && organizer.getProfile().getProfileImage() != null) {
                imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/users/").path(String.valueOf(organizer.getId())).path("/profile-image").toUriString();
            }
            dto.setOrganizer(new UserSummary(organizer.getId(), organizerName, imageUrl));
        }

        dto.setParticipantsCount(event.getParticipants().size());
        if (event.getLocation() != null) {
            dto.setLocation(locationMapper.toDto(event.getLocation()));
        }

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


        return event;
    }
}