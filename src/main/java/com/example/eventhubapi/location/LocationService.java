package com.example.eventhubapi.location;

import com.example.eventhubapi.location.dto.LocationCreationRequest;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.location.exception.LocationNotFoundException;
import com.example.eventhubapi.location.mapper.LocationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
public class LocationService {

    private final LocationRepository locationRepository;
    // Repositories for lookup tables would be injected here
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Transactional
    public LocationDto createLocation(LocationCreationRequest request) {
        // In a real application, you would have logic here to find or create
        // the Country, Region, City, and PostalCode entities based on the request strings.
        // This is a complex task involving potential external API calls or a pre-populated DB.
        // For now, we will assume these entities exist and focus on creating the Location.

        Location location = locationMapper.toEntity(request);
        Location savedLocation = locationRepository.save(location);
        return locationMapper.toDto(savedLocation);
    }

    @Transactional(readOnly = true)
    public Page<LocationDto> getAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable)
                .map(locationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public LocationDto getLocationById(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + locationId));
        return locationMapper.toDto(location);
    }
}