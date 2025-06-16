package org.example.persistence;


import org.example.domain.Entity;
import org.example.business.exception.ValidationException;
import java.util.Optional;


/**
 * Generic interface for CRUD operations on a repository for a specific type.
 *
 * @param <ID> the type of the entity's identifier
 * @param <E>  the type of the entity, which extends Entity<ID>
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Finds a single entity by its ID.
     *
     * @param id the ID of the entity to find; must not be null
     * @return an Optional containing the entity if found, or empty otherwise
     * @throws RepositoryException if a data access error occurs
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all entities from the repository.
     *
     * @return an Iterable of all entities
     * @throws RepositoryException if a data access error occurs
     */
    Iterable<E> getAll();

    /**
     * Adds a new entity to the repository.
     *
     * @param entity the entity to add
     * @return an Optional containing the saved entity, or empty if save failed
     * @throws RepositoryException if a data access error occurs
     */
    Optional<E> add(E entity);


    /**
     * Deletes an entity by its ID.
     *
     * @param id the ID of the entity to delete
     * @return an Optional containing the deleted entity, or empty if not found
     * @throws RepositoryException if a data access error occurs
     */
    Optional<E> delete(ID id);

    /**
     * Updates an existing entity in the repository.
     *
     * @param entity the entity to update
     * @return an Optional containing the updated entity, or empty if not found
     * @throws ValidationException if the entity is invalid
     * @throws RepositoryException if a data access error occurs
     */
    Optional<E> update(E entity);

}




