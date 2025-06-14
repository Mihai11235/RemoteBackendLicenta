package org.example.domain.validators;

import org.example.business.exception.ValidationException;

public interface Validator<T> {
    void validate(T entity) throws ValidationException;
}