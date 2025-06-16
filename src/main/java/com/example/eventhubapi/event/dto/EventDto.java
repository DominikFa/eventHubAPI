package com.example.eventhubapi.event.dto;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.location.dto.LocationDto;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO for exposing detailed event data to the client.
 */
@Getter
@Setter
public class EventDto {
    private Long id;
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private boolean isPublic;
    private Long maxParticipants;
    private LocationDto location;
    private int participantsCount;
    private UserSummary organizer;
}