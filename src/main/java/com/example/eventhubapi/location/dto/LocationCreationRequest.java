package com.example.eventhubapi.location.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationCreationRequest {
    @NotEmpty
    private String streetName;
    @NotEmpty
    private String streetNumber;
    private String apartment;
    @NotEmpty
    private String postalCode;
    @NotEmpty
    private String city;
    @NotEmpty
    private String region;
    @NotEmpty
    private String countryIsoCode;
    private Double latitude;
    private Double longitude;
}