package org.example.web;

import java.time.LocalDateTime;

/**
 * Represents an error response sent to the client.
 * This class encapsulates the details of an error that occurred during request processing.
 */
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;

    /**
     * Constructs an ErrorResponse with the specified status, error, and message.
     *
     * @param status  the HTTP status code
     * @param error   a brief description of the error
     * @param message a detailed message about the error
     */
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}