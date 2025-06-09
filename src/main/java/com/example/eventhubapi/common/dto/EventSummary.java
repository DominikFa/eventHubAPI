package com.example.eventhubapi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A simplified DTO representing an event, used for embedding in other DTOs
 * like Invitation to provide context without the full event details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventSummary {
    private Long id;
    private String name;
    private Instant startDate;
    private Instant endDate;
}