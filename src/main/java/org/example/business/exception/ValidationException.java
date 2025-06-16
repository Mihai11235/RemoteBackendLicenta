package org.example.business.exception;

/**
 * Exception thrown when validation of a business object fails.
 * Extends the ServiceException class to provide specific error handling for validation issues.
 */
public class ValidationException extends ServiceException {
    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message the detail message describing the validation error
     */
    public ValidationException(String message) {
        super(message);
    }
}