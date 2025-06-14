package org.example.business.exception;

public class InvalidCredentialsException extends ServiceException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}