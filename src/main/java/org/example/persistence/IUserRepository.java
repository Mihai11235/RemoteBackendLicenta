package org.example.persistence;
import org.example.domain.User;
import java.util.Optional;

public interface IUserRepository extends Repository<Long, User>{
    Optional<User> findOneByUsername(String username);
}
