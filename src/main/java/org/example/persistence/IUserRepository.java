package org.example.persistence;
import org.example.domain.User;
import java.util.Optional;

/**
 * Interface for managing User entities in the repository.
 * Extends the generic Repository interface.
 */
public interface IUserRepository extends Repository<Long, User>{
    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the User if found, or empty if not
     * @throws RepositoryException if a data access error occurs
     */
    Optional<User> findOneByUsername(String username);
}
