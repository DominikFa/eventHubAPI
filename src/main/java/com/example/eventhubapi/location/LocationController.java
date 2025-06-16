package com.example.eventhubapi.location;

import com.example.eventhubapi.location.dto.LocationCreationRequest;
import com.example.eventhubapi.location.dto.LocationDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * REST controller for managing locations.
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    /**
     * Constructs a LocationController with the necessary LocationService.
     * @param locationService The service for location-related business logic.
     */
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Creates a new location. (Admin/Organizer only)
     * @param request The request body containing the details of the location to be created.
     * @return A ResponseEntity with the created LocationDto and HTTP status 201.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<LocationDto> createLocation(@Valid @RequestBody LocationCreationRequest request) {
        LocationDto createdLocation = locationService.createLocation(request);
        return new ResponseEntity<>(createdLocation, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all locations with optional filters. (Admin/Organizer only)
     * @param pageable Pagination and sorting information.
     * @param streetName Optional filter for street name.
     * @param city Optional filter for city.
     * @param region Optional filter for region.
     * @param countryIsoCode Optional filter for country ISO code.
     * @return A ResponseEntity with a page of LocationDto objects.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Page<LocationDto>> getAllLocations(
            Pageable pageable,
            @RequestParam(required = false) String streetName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String countryIsoCode) {
        Page<LocationDto> locations = locationService.getAllLocations(pageable, streetName, city, region, countryIsoCode);
        return ResponseEntity.ok(locations);
    }

    /**
     * Retrieves a single location by its ID.
     * @param locationId The ID of the location to retrieve.
     * @return A ResponseEntity with the LocationDto.
     */
    @GetMapping("/{locationId}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long locationId) {
        LocationDto location = locationService.getLocationById(locationId);
        return ResponseEntity.ok(location);
    }
}