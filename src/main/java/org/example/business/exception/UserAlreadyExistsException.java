package org.example.business.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 * Extends the ServiceException class to provide specific error handling for user creation conflicts.
 */
public class UserAlreadyExistsException extends ServiceException {
    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}