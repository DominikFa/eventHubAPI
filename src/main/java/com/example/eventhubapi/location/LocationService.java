package com.example.eventhubapi.location;

import com.example.eventhubapi.location.dto.LocationCreationRequest;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.location.exception.LocationNotFoundException;
import com.example.eventhubapi.location.mapper.LocationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;

/**
 * Service class for handling location-related business logic.
 */
@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final PostalCodeRepository postalCodeRepository;
    private final LocationMapper locationMapper;

    /**
     * Constructs a LocationService with the necessary dependencies.
     * @param locationRepository The repository for location data access.
     * @param countryRepository The repository for country data access.
     * @param regionRepository The repository for region data access.
     * @param cityRepository The repository for city data access.
     * @param postalCodeRepository The repository for postal code data access.
     * @param locationMapper The mapper for converting between Location entities and DTOs.
     */
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

    /**
     * Creates a new location based on the provided request.
     * It finds or creates related Country, Region, City, and PostalCode entities.
     * @param request The request DTO containing location creation details.
     * @return A LocationDto representing the newly created location.
     */
    @Transactional
    public LocationDto createLocation(LocationCreationRequest request) {
        Country country = countryRepository.findByIsoCode(request.getCountryIsoCode())
                .orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setIsoCode(request.getCountryIsoCode());
                    newCountry.setName("Unknown");
                    return countryRepository.save(newCountry);
                });

        Region region = regionRepository.findByCodeAndCountry(request.getRegion(), country)
                .orElseGet(() -> {
                    Region newRegion = new Region();
                    newRegion.setCode(request.getRegion());
                    newRegion.setName(request.getRegion());
                    newRegion.setCountry(country);
                    return regionRepository.save(newRegion);
                });

        City city = cityRepository.findByNameAndRegion(request.getCity(), region)
                .orElseGet(() -> {
                    City newCity = new City();
                    newCity.setName(request.getCity());
                    newCity.setRegion(region);
                    newCity.setPostalCodes(new HashSet<>());
                    return cityRepository.save(newCity);
                });

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
        location.setPostalCode(postalCode);

        Location savedLocation = locationRepository.save(location);
        return locationMapper.toDto(savedLocation);
    }

    /**
     * Retrieves a paginated list of all locations, with optional filters.
     * @param pageable Pagination and sorting information.
     * @param streetName Optional filter for street name.
     * @param city Optional filter for city.
     * @param region Optional filter for region.
     * @param countryIsoCode Optional filter for country ISO code.
     * @return A Page of LocationDto objects.
     */
    @Transactional(readOnly = true)
    public Page<LocationDto> getAllLocations(
            Pageable pageable,
            String streetName,
            String city,
            String region,
            String countryIsoCode) {
        Specification<Location> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Location, PostalCode> postalCodeJoin = root.join("postalCode");
            Join<PostalCode, City> cityJoin = postalCodeJoin.join("cities");
            Join<City, Region> regionJoin = cityJoin.join("region");
            Join<Region, Country> countryJoin = regionJoin.join("country");

            if (streetName != null && !streetName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("streetName")), "%" + streetName.toLowerCase() + "%"));
            }
            if (city != null && !city.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(cityJoin.get("name")), "%" + city.toLowerCase() + "%"));
            }
            if (region != null && !region.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(regionJoin.get("name")), "%" + region.toLowerCase() + "%"));
            }
            if (countryIsoCode != null && !countryIsoCode.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(countryJoin.get("isoCode")), "%" + countryIsoCode.toLowerCase() + "%"));
            }

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return locationRepository.findAll(spec, pageable)
                .map(locationMapper::toDto);
    }

    /**
     * Retrieves a single location by its ID.
     * @param locationId The ID of the location.
     * @return A LocationDto representing the location.
     */
    @Transactional(readOnly = true)
    public LocationDto getLocationById(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + locationId));
        return locationMapper.toDto(location);
    }
}