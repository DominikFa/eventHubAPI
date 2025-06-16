package com.example.eventhubapi.location.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for exposing location data to the client.
 */
@Getter
@Setter
public class LocationDto {
    private Long id;
    private String streetName;
    private String streetNumber;
    private String apartment;
    private String postalCode;
    private String city;
    private String region;
    private String countryIsoCode;
    private Double latitude;
    private Double longitude;
    private String fullAddress;
}