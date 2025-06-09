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
import com.example.eventhubapi.location.exception.LocationNotFoundException;
import com.example.eventhubapi.security.Role;
import com.example.eventhubapi.security.RoleRepository;
import com.example.eventhubapi.security.exception.RoleNotFoundException;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.enums.AccountStatus;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public AdminService(UserRepository userRepository,
                        EventRepository eventRepository,
                        MediaRepository mediaRepository,
                        RoleRepository roleRepository,
                        LocationRepository locationRepository,
                        UserMapper userMapper,
                        EventMapper eventMapper) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.mediaRepository = mediaRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.userMapper = userMapper;
        this.eventMapper = eventMapper;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUserStatus(Long userId, AccountStatus newStatus) {
        User user = findUserById(userId);
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
    public EventDto updateAnyEvent(Long eventId, AdminEventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setPublic(request.isPublic());
        event.setMaxParticipants(request.getMaxParticipants());

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + request.getLocationId()));
            event.setLocation(location);
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