package org.example.domain.validators;

import org.example.business.exception.ValidationException;

/**
 * Interface for validating entities.
 *
 * @param <T> the type of entity to validate
 */
public interface Validator<T> {
    /**
     * Validates the given entity.
     *
     * @param entity the entity to validate
     * @throws ValidationException if the entity is not valid
     */
    void validate(T entity) throws ValidationException;
}