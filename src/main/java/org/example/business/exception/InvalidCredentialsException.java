package org.example.business.exception;

/**
 * Exception thrown when invalid credentials are provided during authentication.
 * Extends the ServiceException class to provide specific error handling for authentication failures.
 */
public class InvalidCredentialsException extends ServiceException {
    /**
     * Constructs a new InvalidCredentialsException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}