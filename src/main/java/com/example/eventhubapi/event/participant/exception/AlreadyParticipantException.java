package com.example.eventhubapi.event.participant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to join an event they are already a part of.
 * Results in an HTTP 409 Conflict status.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyParticipantException extends RuntimeException {
    public AlreadyParticipantException(String message) {
        super(message);
    }
}