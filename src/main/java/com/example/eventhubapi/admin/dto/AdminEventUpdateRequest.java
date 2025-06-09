package com.example.eventhubapi.admin.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO for an admin to update an event's details.
 */
@Getter
@Setter
public class AdminEventUpdateRequest {
    @NotEmpty
    private String name;
    private String description;
    @NotNull @Future
    private Instant startDate;
    @NotNull @Future
    private Instant endDate;
    @NotNull
    private boolean isPublic;
    private Long maxParticipants;
    private Long locationId;
}