package com.example.eventhubapi.event.mapper;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.participant.ParticipantRepository;
import com.example.eventhubapi.event.participant.enums.ParticipantStatus;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import com.example.eventhubapi.location.mapper.LocationMapper;

/**
 * Service class for mapping between Event entities and their DTOs.
 */
@Service
public class EventMapper {

    private final LocationMapper locationMapper;
    private final UserMapper userMapper;
    private final ParticipantRepository participantRepository;

    public EventMapper(LocationMapper locationMapper, UserMapper userMapper, ParticipantRepository participantRepository) {
        this.locationMapper = locationMapper;
        this.userMapper = userMapper;
        this.participantRepository = participantRepository;
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
            dto.setOrganizer(userMapper.toUserSummary(event.getOrganizer()));
        }

        long attendingCount = participantRepository.countByEventIdAndStatus(event.getId(), ParticipantStatus.ATTENDING);
        dto.setParticipantsCount((int) attendingCount);

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