package com.example.eventhubapi.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EventCreationRequest {
    @NotEmpty
    private String name;
    private String description;
    @NotNull @Future
    private Instant startDate;
    @NotNull @Future
    private Instant endDate;
    @NotNull
    private Boolean isPublic;
    private Long maxParticipants;
    private Long locationId; // Assuming location is created separately and ID is provided
}