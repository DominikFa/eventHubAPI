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

/**
 * DTO for capturing the data needed to create or update an event.
 * Includes validation for exclusive location fields.
 */
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
    private Long locationId;
    @Valid
    private LocationCreationRequest location;
}