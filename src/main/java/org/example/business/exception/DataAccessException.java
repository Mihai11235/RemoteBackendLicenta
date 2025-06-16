package org.example.business.exception;

/**
 * Exception thrown when there is an issue accessing data in the persistence layer.
 * Extends the ServiceException class to provide specific error handling for data access problems.
 */
public class DataAccessException extends ServiceException {
    /**
     * Constructs a new DataAccessException with the specified detail message.
     *
     * @param message the detail message
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}