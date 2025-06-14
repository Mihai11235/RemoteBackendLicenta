package org.example.business.exception;

public class DataAccessException extends ServiceException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}