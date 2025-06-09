package com.example.eventhubapi.event.participant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyParticipantException extends RuntimeException {
    public AlreadyParticipantException(String message) {
        super(message);
    }
}