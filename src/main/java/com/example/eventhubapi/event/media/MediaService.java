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

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final MediaMapper mediaMapper;

    public MediaService(MediaRepository mediaRepository, EventRepository eventRepository, ParticipantRepository participantRepository, MediaMapper mediaMapper) {
        this.mediaRepository = mediaRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.mediaMapper = mediaMapper;
    }

    // --- GALLERY LOGIC ---
    @Transactional
    public MediaDto addGalleryImage(Long eventId, MultipartFile file, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin"));
        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());
        boolean isParticipant = participantRepository.findByEventIdAndUserId(eventId, currentUser.getId()).isPresent();

        if (!isAdmin && !isOrganizer && !isParticipant) {
            throw new AccessDeniedException("You must be a participant or the organizer to upload gallery images.");
        }

        Media savedMedia = store(file, event, MediaUsage.GALLERY, currentUser);
        return mediaMapper.toDto(savedMedia);
    }

    @Transactional
    public void deleteGalleryImage(Long eventId, Long mediaId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Media media = findMediaOrThrow(mediaId, eventId);

        if (media.getUsage() != MediaUsage.GALLERY) {
            throw new IllegalArgumentException("This media item is not a gallery image.");
        }

        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin"));
        boolean isOrganizer = media.getEvent().getOrganizer().getId().equals(currentUser.getId());
        boolean isUploader = media.getUploader() != null && media.getUploader().getId().equals(currentUser.getId());

        if (isAdmin || isOrganizer || isUploader) {
            mediaRepository.delete(media);
        } else {
            throw new AccessDeniedException("You do not have permission to delete this gallery image.");
        }
    }

    // --- LOGO & SCHEDULE LOGIC ---
    @Transactional
    public MediaDto uploadEventLogo(Long eventId, MultipartFile file, Authentication authentication) throws IOException {
        Media savedMedia = uploadRestrictedMedia(eventId, file, authentication, MediaUsage.LOGO);
        return mediaMapper.toDto(savedMedia);
    }

    @Transactional
    public MediaDto uploadEventSchedule(Long eventId, MultipartFile file, Authentication authentication) throws IOException {
        Media savedMedia = uploadRestrictedMedia(eventId, file, authentication, MediaUsage.SCHEDULE);
        return mediaMapper.toDto(savedMedia);
    }

    @Transactional
    public void deleteRestrictedMedia(Long eventId, MediaUsage usage, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        authorizeOrganizerOrAdmin(event, currentUser);

        mediaRepository.findOneByEventIdAndUsage(eventId, usage).ifPresent(mediaRepository::delete);
    }

    // --- GENERIC GETTER ---
    @Transactional(readOnly = true)
    public Media getMediaFile(Long mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + mediaId));
    }

    // --- PRIVATE HELPER METHODS ---
    private Media uploadRestrictedMedia(Long eventId, MultipartFile file, Authentication authentication, MediaUsage usage) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        authorizeOrganizerOrAdmin(event, currentUser);

        mediaRepository.findOneByEventIdAndUsage(eventId, usage).ifPresent(mediaRepository::delete);

        return store(file, event, usage, currentUser);
    }

    private Media store(MultipartFile file, Event event, MediaUsage usage, User uploader) throws IOException {
        Media media = new Media();
        media.setEvent(event);
        media.setUploader(uploader);
        media.setMediaFile(file.getBytes());
        media.setMediaType(MediaType.fromValue(file.getContentType()));
        media.setUsage(usage);
        media.setUploadedAt(Instant.now());
        return mediaRepository.save(media);
    }

    private void authorizeOrganizerOrAdmin(Event event, User user) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin"));
        boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
        if (!isAdmin && !isOrganizer) {
            throw new AccessDeniedException("You must be the event organizer or an admin to perform this action.");
        }
    }

    private Media findMediaOrThrow(Long mediaId, Long eventId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + mediaId));
        if (!media.getEvent().getId().equals(eventId)) {
            throw new AccessDeniedException("This media item does not belong to the specified event.");
        }
        return media;
    }
}