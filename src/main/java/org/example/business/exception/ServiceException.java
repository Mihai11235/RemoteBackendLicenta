package org.example.business.exception;

/**
 * Exception thrown when there is an issue in the service layer.
 * This could be due to business logic errors, validation failures, etc.
 */
public class ServiceException extends RuntimeException {
    /**
     * Constructs a new ServiceException with the specified detail message.
     *
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new ServiceException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}