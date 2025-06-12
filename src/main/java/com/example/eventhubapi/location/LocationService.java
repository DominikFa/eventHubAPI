package com.example.eventhubapi.location;

import com.example.eventhubapi.location.dto.LocationCreationRequest;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.location.exception.LocationNotFoundException;
import com.example.eventhubapi.location.mapper.LocationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;



@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final PostalCodeRepository postalCodeRepository;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository,
                           CountryRepository countryRepository,
                           RegionRepository regionRepository,
                           CityRepository cityRepository,
                           PostalCodeRepository postalCodeRepository,
                           LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.countryRepository = countryRepository;
        this.regionRepository = regionRepository;
        this.cityRepository = cityRepository;
        this.postalCodeRepository = postalCodeRepository;
        this.locationMapper = locationMapper;
    }

    @Transactional
    public LocationDto createLocation(LocationCreationRequest request) {
        // Find or create Country
        Country country = countryRepository.findByIsoCode(request.getCountryIsoCode())
                .orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setIsoCode(request.getCountryIsoCode());
                    newCountry.setName("Unknown"); // Name can be enriched later
                    return countryRepository.save(newCountry);
                });

        // Find or create Region
        Region region = regionRepository.findByCodeAndCountry(request.getRegion(), country)
                .orElseGet(() -> {
                    Region newRegion = new Region();
                    newRegion.setCode(request.getRegion());
                    newRegion.setName(request.getRegion()); // Assuming code is the name for simplicity
                    newRegion.setCountry(country);
                    return regionRepository.save(newRegion);
                });

        // Find or create City
        City city = cityRepository.findByNameAndRegion(request.getCity(), region)
                .orElseGet(() -> {
                    City newCity = new City();
                    newCity.setName(request.getCity());
                    newCity.setRegion(region);
                    newCity.setPostalCodes(new HashSet<>());
                    return cityRepository.save(newCity);
                });

        // Find or create PostalCode and associate with City
        PostalCode postalCode = postalCodeRepository.findByCode(request.getPostalCode())
                .orElseGet(() -> {
                    PostalCode newPostalCode = new PostalCode();
                    newPostalCode.setCode(request.getPostalCode());
                    return postalCodeRepository.save(newPostalCode);
                });

        if (!city.getPostalCodes().contains(postalCode)) {
            city.getPostalCodes().add(postalCode);
            cityRepository.save(city);
        }


        Location location = locationMapper.toEntity(request);
        location.setPostalCode(postalCode); // Set the managed postal code entity

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