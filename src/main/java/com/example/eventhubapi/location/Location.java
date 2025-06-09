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

    @Embedded
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