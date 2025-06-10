package com.example.eventhubapi.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An entity class representing geographic coordinates.
 */
@Entity
@Table(name = "map_location")
@Getter
@Setter
@NoArgsConstructor
public class MapLocation {
    @Id
    @Column(name = "location_id")
    private Long id;

    @Column(columnDefinition = "numeric")
    private Double latitude;

    @Column(columnDefinition = "numeric")
    private Double longitude;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "location_id")
    private Location location;
}