package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.media.dto.MediaDto;
import com.example.eventhubapi.event.media.enums.MediaType;
import com.example.eventhubapi.event.media.enums.MediaUsage;
import com.example.eventhubapi.event.media.exception.MediaNotFoundException;
import com.example.eventhubapi.event.media.mapper.MediaMapper;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final MediaMapper mediaMapper;

    public MediaService(MediaRepository mediaRepository, EventRepository eventRepository, UserRepository userRepository, MediaMapper mediaMapper) {
        this.mediaRepository = mediaRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.mediaMapper = mediaMapper;
    }

    @Transactional
    public MediaDto store(MultipartFile file, Long eventId, String usageStr, String uploaderEmail) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + uploaderEmail));

        Media media = new Media();
        media.setEvent(event);
        media.setUploader(uploader);
        media.setMediaFile(file.getBytes()); // Store the actual file bytes
        media.setMediaType(MediaType.valueOf(file.getContentType().toUpperCase().replace("/", "_")));
        media.setUsage(MediaUsage.valueOf(usageStr.toUpperCase()));
        media.setUploadedAt(Instant.now());

        Media savedMedia = mediaRepository.save(media);
        return mediaMapper.toDto(savedMedia);
    }

    @Transactional(readOnly = true)
    public List<MediaDto> getMediaForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found with id: " + eventId);
        }
        List<Media> mediaList = mediaRepository.findByEventId(eventId);
        return mediaList.stream()
                .map(mediaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a media entity by its ID.
     *
     * @param mediaId The ID of the media file.
     * @return The Media entity containing the file data.
     */
    @Transactional(readOnly = true)
    public Media getMediaFile(Long mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + mediaId));
    }
}