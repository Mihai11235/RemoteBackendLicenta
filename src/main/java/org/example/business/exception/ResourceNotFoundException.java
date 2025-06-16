package org.example.business.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Extends the ServiceException class to provide specific error handling for resource retrieval failures.
 */
public class ResourceNotFoundException extends ServiceException {
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}