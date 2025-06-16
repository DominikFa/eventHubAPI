package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.media.dto.MediaDto;
import com.example.eventhubapi.event.media.enums.MediaType;
import com.example.eventhubapi.event.media.enums.MediaUsage;
import com.example.eventhubapi.event.media.exception.MediaNotFoundException;
import com.example.eventhubapi.event.media.mapper.MediaMapper;
import com.example.eventhubapi.event.participant.ParticipantRepository;
import com.example.eventhubapi.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

/**
 * Service class for handling media-related business logic.
 */
@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final MediaMapper mediaMapper;

    /**
     * Constructs a MediaService with the necessary dependencies.
     *
     * @param mediaRepository       The repository for media data access.
     * @param eventRepository       The repository for event data access.
     * @param participantRepository The repository for participant data access.
     * @param mediaMapper           The mapper for converting between Media entities and DTOs.
     */
    public MediaService(MediaRepository mediaRepository, EventRepository eventRepository, ParticipantRepository participantRepository, MediaMapper mediaMapper) {
        this.mediaRepository = mediaRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.mediaMapper = mediaMapper;
    }

    private void authorizeOrganizerOrAdmin(Event event, User user) {
        boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("admin"));
        if (!isOrganizer && !isAdmin) {
            throw new AccessDeniedException("You must be the event organizer or an admin to perform this action.");
        }
    }

    /**
     * Uploads a gallery image for an event.
     * @param eventId The ID of the event.
     * @param file The image file to upload.
     * @param authentication The authentication object of the current user.
     * @return A DTO of the created media.
     * @throws IOException If an I/O error occurs.
     */
    @Transactional
    public MediaDto uploadGalleryImage(Long eventId, MultipartFile file, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Event event = findEventById(eventId);

        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());
        boolean isParticipant = participantRepository.findByEventIdAndUserId(eventId, currentUser.getId()).isPresent();

        if (!isParticipant && !isOrganizer) {
            throw new AccessDeniedException("You must be a participant or the event organizer to upload gallery images.");
        }

        return storeAndMap(file, event, MediaUsage.GALLERY, currentUser);
    }

    /**
     * Uploads a logo for an event.
     * @param eventId The ID of the event.
     * @param file The logo file to upload.
     * @param authentication The authentication object of the current user.
     * @return A DTO of the created media.
     * @throws IOException If an I/O error occurs.
     */
    @Transactional
    public MediaDto uploadLogo(Long eventId, MultipartFile file, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Event event = findEventById(eventId);
        authorizeOrganizerOrAdmin(event, currentUser);

        mediaRepository.findOneByEventIdAndUsage(eventId, MediaUsage.LOGO).ifPresent(mediaRepository::delete);
        return storeAndMap(file, event, MediaUsage.LOGO, currentUser);
    }

    /**
     * Uploads a schedule for an event.
     * @param eventId The ID of the event.
     * @param file The schedule file to upload.
     * @param authentication The authentication object of the current user.
     * @return A DTO of the created media.
     * @throws IOException If an I/O error occurs.
     */
    @Transactional
    public MediaDto uploadSchedule(Long eventId, MultipartFile file, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Event event = findEventById(eventId);
        authorizeOrganizerOrAdmin(event, currentUser);

        return storeAndMap(file, event, MediaUsage.SCHEDULE, currentUser);
    }

    /**
     * Uploads a gallery image for an event as an administrator.
     * @param eventId The ID of the event.
     * @param file The image file to upload.
     * @param authentication The authentication object of the current user.
     * @return A DTO of the created media.
     * @throws IOException If an I/O error occurs.
     */
    @Transactional
    public MediaDto adminUploadGalleryImage(Long eventId, MultipartFile file, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Event event = findEventById(eventId);
        return storeAndMap(file, event, MediaUsage.GALLERY, currentUser);
    }

    /**
     * Retrieves a media file by its ID.
     * @param mediaId The ID of the media file.
     * @return The Media entity.
     */
    @Transactional(readOnly = true)
    public Media getMediaFile(Long mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + mediaId));
    }

    /**
     * Retrieves a schedule file by its ID, ensuring the user is a participant.
     * @param mediaId The ID of the schedule file.
     * @param authentication The authentication object of the current user.
     * @return The Media entity.
     */
    @Transactional(readOnly = true)
    public Media getScheduleFile(Long mediaId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Media media = getMediaFile(mediaId);
        Event event = media.getEvent();

        boolean isParticipant = participantRepository.findByEventIdAndUserId(event.getId(), currentUser.getId()).isPresent();
        if (!isParticipant) {
            throw new AccessDeniedException("You must be a participant to download the schedule.");
        }
        return media;
    }

    /**
     * Deletes a user's own gallery media.
     * @param mediaId The ID of the media file to delete.
     * @param authentication The authentication object of the current user.
     */
    @Transactional
    public void deleteOwnGalleryMedia(Long mediaId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Media media = getMediaFile(mediaId);

        if (media.getUploader() == null || !media.getUploader().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only delete your own media.");
        }
        mediaRepository.delete(media);
    }

    /**
     * Allows an organizer to delete any gallery media from their event.
     * @param eventId The ID of the event.
     * @param mediaId The ID of the media file to delete.
     * @param authentication The authentication object of the current user.
     */
    @Transactional
    public void organizerDeleteGalleryMedia(Long eventId, Long mediaId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Event event = findEventById(eventId);
        authorizeOrganizerOrAdmin(event, currentUser);

        Media media = getMediaFile(mediaId);
        if(!Objects.equals(media.getEvent().getId(), eventId)) {
            throw new AccessDeniedException("Media does not belong to this event.");
        }
        mediaRepository.delete(media);
    }

    /**
     * Deletes a media file by its ID (Admin only).
     * @param mediaId The ID of the media file to delete.
     */
    @Transactional
    public void adminDeleteMedia(Long mediaId) {
        if (!mediaRepository.existsById(mediaId)) {
            throw new MediaNotFoundException("Media not found with id: " + mediaId);
        }
        mediaRepository.deleteById(mediaId);
    }


    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
    }

    private MediaDto storeAndMap(MultipartFile file, Event event, MediaUsage usage, User uploader) throws IOException {
        Media media = new Media();
        media.setEvent(event);
        media.setUploader(uploader);
        media.setMediaFile(file.getBytes());
        media.setMediaType(MediaType.fromValue(file.getContentType()));
        media.setUsage(usage);
        media.setUploadedAt(Instant.now());
        Media savedMedia = mediaRepository.save(media);
        return mediaMapper.toDto(savedMedia);
    }
}