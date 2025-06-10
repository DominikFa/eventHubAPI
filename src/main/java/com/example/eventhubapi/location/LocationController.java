package com.example.eventhubapi.location;

import com.example.eventhubapi.location.dto.LocationCreationRequest;
import com.example.eventhubapi.location.dto.LocationDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<LocationDto> createLocation(@Valid @RequestBody LocationCreationRequest request) {
        LocationDto createdLocation = locationService.createLocation(request);
        return new ResponseEntity<>(createdLocation, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        List<LocationDto> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long locationId) {
        LocationDto location = locationService.getLocationById(locationId);
        return ResponseEntity.ok(location);
    }
}