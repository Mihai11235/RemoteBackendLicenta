package org.example.persistence;
import org.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

/**
 * Repository class for managing User entities in the database.
 */
@Component
public class UserRepository implements IUserRepository{
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        return user;
    };

    @Autowired
    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<User> findOne(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            // queryForObject is used when you expect exactly one result
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        catch (DataAccessException e) {
            throw new RepositoryException("UserRepository: Failed to find user by id", e);
        }
    }


    @Override
    public Optional<User> findOneByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        catch (DataAccessException e) {
            throw new RepositoryException("UserRepository: Failed to find user by username", e);
        }
    }

    @Override
    public Iterable<User> getAll() {
        try {
            String sql = "SELECT * FROM users";
            return jdbcTemplate.query(sql, userRowMapper);
        } catch (DataAccessException e) {
            throw new RepositoryException("UserRepository: Failed to get all users", e);
        }
    }

    @Override
    public Optional<User> add(User entity) {
        try {
            String sql = "INSERT INTO users (username, password, name) VALUES (?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, entity.getUsername());
                preparedStatement.setString(2, entity.getPassword());
                preparedStatement.setString(3, entity.getName());
                return preparedStatement;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                entity.setId(keyHolder.getKey().longValue());
                return Optional.of(entity);
            } else {
                return Optional.empty();
            }
        } catch (DataAccessException e) {
            throw new RepositoryException("UserRepository: Failed to add user", e);
        }
    }

    @Override
    public Optional<User> delete(Long aLong) {
        try {
            Optional<User> entityOpt = findOne(aLong);

            if (entityOpt.isEmpty()) {
                return Optional.empty();
            }

            int affectedRows = jdbcTemplate.update("DELETE FROM users where id=?", aLong);

            return affectedRows > 0 ? entityOpt : Optional.empty();
        } catch (DataAccessException e) {
            throw new RepositoryException("UserRepository: Failed to delete user", e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        return Optional.empty();
    }
}
