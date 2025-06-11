package com.example.eventhubapi.event.dto;

import com.example.eventhubapi.common.validator.ExclusiveFields;
import com.example.eventhubapi.location.dto.LocationCreationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@ExclusiveFields(value = {"locationId", "location"}, message = "Provide either a locationId or a new location object, but not both.")
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

    // Option 1: Link to an existing location
    private Long locationId;

    // Option 2: Create a new location
    @Valid
    private LocationCreationRequest location;
}