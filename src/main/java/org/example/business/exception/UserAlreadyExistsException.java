package org.example.business.exception;

// New exception for HTTP 409 Conflict
public class UserAlreadyExistsException extends ServiceException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}