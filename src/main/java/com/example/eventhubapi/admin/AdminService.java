package com.example.eventhubapi.admin;

import com.example.eventhubapi.admin.dto.AdminEventUpdateRequest;
import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.mapper.EventMapper;
import com.example.eventhubapi.event.media.MediaRepository;
import com.example.eventhubapi.event.media.exception.MediaNotFoundException;
import com.example.eventhubapi.location.Location;
import com.example.eventhubapi.location.LocationRepository;
import com.example.eventhubapi.location.LocationService;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.location.exception.LocationNotFoundException;
import com.example.eventhubapi.security.Role;
import com.example.eventhubapi.security.RoleRepository;
import com.example.eventhubapi.security.exception.RoleNotFoundException;
import com.example.eventhubapi.user.AccountStatus;
import com.example.eventhubapi.user.AccountStatusRepository;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling administrative business logic.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final MediaRepository mediaRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;
    private final UserMapper userMapper;
    private final EventMapper eventMapper;
    private final AccountStatusRepository accountStatusRepository;
    private final LocationService locationService;

    public AdminService(UserRepository userRepository,
                        EventRepository eventRepository,
                        MediaRepository mediaRepository,
                        RoleRepository roleRepository,
                        LocationRepository locationRepository,
                        UserMapper userMapper,
                        EventMapper eventMapper,
                        AccountStatusRepository accountStatusRepository,
                        LocationService locationService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.mediaRepository = mediaRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.userMapper = userMapper;
        this.eventMapper = eventMapper;
        this.accountStatusRepository = accountStatusRepository;
        this.locationService = locationService;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserDto);
    }

    @Transactional
    public UserDto updateUserStatus(Long userId, String newStatusName) {
        User user = findUserById(userId);

        AccountStatus newStatus = accountStatusRepository.findByStatusName(newStatusName.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + newStatusName));

        user.setStatus(newStatus);

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto changeUserRole(Long userId, String roleName) {
        User user = findUserById(userId);
        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));
        user.setRole(newRole);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public EventDto updateAnyEvent(Long eventId, AdminEventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setPublic(request.getIsPublic());
        event.setMaxParticipants(request.getMaxParticipants());

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + request.getLocationId()));
            event.setLocation(location);
        } else if (request.getLocation() != null) {
            LocationDto createdLocationDto = locationService.createLocation(request.getLocation());
            Location newLocation = locationRepository.findById(createdLocationDto.getId())
                    .orElseThrow(() -> new LocationNotFoundException("Could not find newly created location with id: " + createdLocationDto.getId()));
            event.setLocation(newLocation);
        }


        return eventMapper.toDto(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found with id: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }

    @Transactional
    public void deleteMedia(Long mediaId) {
        if (!mediaRepository.existsById(mediaId)) {
            throw new MediaNotFoundException("Media not found with id: " + mediaId);
        }
        mediaRepository.deleteById(mediaId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
}