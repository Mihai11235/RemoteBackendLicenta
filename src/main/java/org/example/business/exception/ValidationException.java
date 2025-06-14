package org.example.business.exception;

public class ValidationException extends ServiceException {
    public ValidationException(String message) {
        super(message);
    }
}