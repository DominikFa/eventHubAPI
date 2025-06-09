package com.example.eventhubapi.location;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An embeddable class representing geographic coordinates.
 * This will be part of the Location entity and not have its own table.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class MapLocation {
    private Double latitude;
    private Double longitude;
}