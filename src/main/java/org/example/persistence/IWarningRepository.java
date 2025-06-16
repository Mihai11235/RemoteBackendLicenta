package org.example.persistence;
import org.example.domain.Warning;

/**
 * Interface for managing Warning entities in the repository.
 * Inherits all CRUD operations from the generic {@link Repository} interface.
 */
public interface IWarningRepository extends Repository<Long, Warning> {

}
