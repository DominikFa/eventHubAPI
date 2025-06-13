package com.example.eventhubapi.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a physical location for an event.
 */
@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "street_number")
    private String streetNumber;

    private String apartment;

    // The MapLocation entity maps to the same primary key as Location (location_id).
    // It's the "owning" side of the OneToOne relationship on the database schema
    // since it contains the foreign key from location.
    // However, in JPA, MapsId is used when the foreign key column is also the primary key.
    // The cascade is needed here to save MapLocation when Location is saved.
    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MapLocation mapLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postal_code_id")
    private PostalCode postalCode;

    /**
     * A derived property to get the full address as a single string.
     * This logic would be more complex in a real application.
     * @return The formatted full address.
     */
    public String getFullAddress() {
        // This is a simplified example.
        // In a real app, you would build this from the related entities.
        return String.format("%s %s, %s",
                this.streetName,
                this.streetNumber,
                this.postalCode != null ? this.postalCode.getCode() : ""
        );
    }
}
