package com.example.eventhubapi.location.mapper;

import com.example.eventhubapi.location.*;
import com.example.eventhubapi.location.dto.LocationCreationRequest;
import com.example.eventhubapi.location.dto.LocationDto;
import org.springframework.stereotype.Service;

/**
 * Service class for mapping between Location entities and their corresponding DTOs.
 */
@Service
public class LocationMapper {

    public LocationDto toDto(Location location) {
        if (location == null) return null;

        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setStreetName(location.getStreetName());
        dto.setStreetNumber(location.getStreetNumber());
        dto.setApartment(location.getApartment());
        dto.setFullAddress(location.getFullAddress());

        if (location.getMapLocation() != null) {
            dto.setLatitude(location.getMapLocation().getLatitude());
            dto.setLongitude(location.getMapLocation().getLongitude());
        }

        if (location.getPostalCode() != null) {
            PostalCode pc = location.getPostalCode();
            dto.setPostalCode(pc.getCode());
            if (pc.getCities() != null && !pc.getCities().isEmpty()) {
                City city = pc.getCities().iterator().next();
                dto.setCity(city.getName());
                if (city.getRegion() != null) {
                    Region region = city.getRegion();
                    dto.setRegion(region.getName());
                    if (region.getCountry() != null) {
                        dto.setCountryIsoCode(region.getCountry().getIsoCode());
                    }
                }
            }
        }
        return dto;
    }

    public Location toEntity(LocationCreationRequest request) {
        if (request == null) return null;

        Location location = new Location();
        location.setStreetName(request.getStreetName());
        location.setStreetNumber(request.getStreetNumber());
        location.setApartment(request.getApartment());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            MapLocation mapLocation = new MapLocation();
            mapLocation.setLatitude(request.getLatitude());
            mapLocation.setLongitude(request.getLongitude());

            mapLocation.setLocation(location);

            location.setMapLocation(mapLocation);
        }

        return location;
    }
}