package com.example.eventhubapi.common.exception;

import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.media.exception.MediaNotFoundException;
import com.example.eventhubapi.event.participant.exception.AlreadyParticipantException;
import com.example.eventhubapi.event.participant.exception.NotParticipantException;
import com.example.eventhubapi.invitation.exception.InvitationNotFoundException;
import com.example.eventhubapi.location.exception.LocationNotFoundException;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Catches specified exceptions and formats them into a standard ErrorResponse.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions for request bodies.
     * @param ex The caught MethodArgumentNotValidException.
     * @param request The current web request.
     * @return A ResponseEntity containing the formatted error response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Validation Failed", errors, request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles resource not found exceptions.
     * @param ex The caught RuntimeException.
     * @param request The current web request.
     * @return A ResponseEntity with a 404 status and formatted error.
     */
    @ExceptionHandler({UserNotFoundException.class, EventNotFoundException.class, LocationNotFoundException.class, MediaNotFoundException.class, InvitationNotFoundException.class, NotParticipantException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "Resource Not Found", ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles conflict exceptions (e.g., trying to create an existing resource).
     * @param ex The caught RuntimeException.
     * @param request The current web request.
     * @return A ResponseEntity with a 409 status and formatted error.
     */
    @ExceptionHandler({AlreadyParticipantException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), HttpStatus.CONFLICT.value(), "Request Cannot Be Processed", ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles authentication exceptions.
     * @param ex The caught AuthenticationException.
     * @param request The current web request.
     * @return A ResponseEntity with a 401 status and formatted error.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles access denied exceptions.
     * @param ex The caught AccessDeniedException.
     * @param request The current web request.
     * @return A ResponseEntity with a 403 status and formatted error.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles all other uncaught exceptions.
     * @param ex The caught Exception.
     * @param request The current web request.
     * @return A ResponseEntity with a 500 status and formatted error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", ex.getMessage(), request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}