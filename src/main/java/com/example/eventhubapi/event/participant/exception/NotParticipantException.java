package com.example.eventhubapi.event.participant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an action is attempted that requires event participation,
 * but the user is not a participant. Results in an HTTP 404 Not Found status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotParticipantException extends RuntimeException {
    public NotParticipantException(String message) {
        super(message);
    }
}