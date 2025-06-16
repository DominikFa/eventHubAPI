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
import com.example.eventhubapi.event.participant.ParticipantRepository;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ParticipantRepository participantRepository;

    /**
     * Constructs an AdminService with the necessary repositories and mappers.
     * @param userRepository The repository for user data access.
     * @param eventRepository The repository for event data access.
     * @param mediaRepository The repository for media data access.
     * @param roleRepository The repository for role data access.
     * @param locationRepository The repository for location data access.
     * @param userMapper The mapper for converting user entities to DTOs.
     * @param eventMapper The mapper for converting event entities to DTOs.
     * @param accountStatusRepository The repository for account status data access.
     * @param locationService The service for location-related business logic.
     * @param participantRepository The repository for participant data access.
     */
    public AdminService(UserRepository userRepository,
                        EventRepository eventRepository,
                        MediaRepository mediaRepository,
                        RoleRepository roleRepository,
                        LocationRepository locationRepository,
                        UserMapper userMapper,
                        EventMapper eventMapper,
                        AccountStatusRepository accountStatusRepository,
                        LocationService locationService,
                        ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.mediaRepository = mediaRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.userMapper = userMapper;
        this.eventMapper = eventMapper;
        this.accountStatusRepository = accountStatusRepository;
        this.locationService = locationService;
        this.participantRepository = participantRepository;
    }

    /**
     * Updates the status of a user's account.
     * @param userId The ID of the user to update.
     * @param newStatusName The new status name.
     * @return The updated UserDto.
     */
    @Transactional
    public UserDto updateUserStatus(Long userId, String newStatusName) {
        User user = findUserById(userId);

        AccountStatus newStatus = accountStatusRepository.findByStatusName(newStatusName.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + newStatusName));

        user.setStatus(newStatus);

        return userMapper.toUserDto(userRepository.save(user));
    }

    /**
     * Changes the role of a user.
     * @param userId The ID of the user whose role is to be changed.
     * @param roleName The name of the new role.
     * @return The updated UserDto.
     */
    @Transactional
    public UserDto changeUserRole(Long userId, String roleName) {
        User user = findUserById(userId);
        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));
        user.setRole(newRole);
        return userMapper.toUserDto(userRepository.save(user));
    }

    /**
     * Deletes a user by their ID.
     * @param userId The ID of the user to delete.
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Updates any event's details.
     * @param eventId The ID of the event to update.
     * @param request The request object containing the new event details.
     * @return The updated EventDto.
     */
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

    /**
     * Deletes an event by its ID.
     * @param eventId The ID of the event to delete.
     */
    @Transactional
    public void deleteEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        eventRepository.delete(event);
    }

    /**
     * Deletes a media file by its ID.
     * @param mediaId The ID of the media file to delete.
     */
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