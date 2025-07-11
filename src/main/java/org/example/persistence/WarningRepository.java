package org.example.persistence;
import org.example.domain.Warning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

/**
 * Repository class for managing Report entities in the database.
 */
@Component
public class WarningRepository implements IWarningRepository{

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WarningRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<Warning> findOne(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Iterable<Warning> getAll() {
        return null;
    }

    /**
     * Adds a new Warning entity to the database.
     * If the entity has a non-null created_at timestamp, it will be inserted; otherwise, it is omitted.
     *
     * @param entity the Warning to be added
     * @return an Optional containing the inserted Warning with generated ID, or empty if insert failed
     * @throws RepositoryException if a database access error occurs
     */
    @Override
    public Optional<Warning> add(Warning entity) {
        try {
            String sqlWithTimestamp = "INSERT INTO warnings (report_id, text, lat, lng, created_at) VALUES (?,?,?,?,?)";
            String sqlWithoutTimestamp = "INSERT INTO warnings (report_id, text, lat, lng) VALUES (?,?,?,?)";
            String sql = entity.getCreated_at() != null ? sqlWithTimestamp : sqlWithoutTimestamp;

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, entity.getReport_id());
                ps.setString(2, entity.getText());
                ps.setDouble(3, entity.getLat());
                ps.setDouble(4, entity.getLng());
                if (entity.getCreated_at() != null) {
                    ps.setLong(5, entity.getCreated_at());
                }
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                entity.setId(keyHolder.getKey().longValue());
                return Optional.of(entity);
            } else {
                return Optional.empty();
            }
        } catch (DataAccessException e) {
            throw new RepositoryException("WarningRepository: Failed to add warning", e);
        }
    }

    /**
     * Deletes the Warning with the given ID from the database.
     *
     * @param aLong the ID of the Warning to delete
     * @return an Optional containing the deleted entity, or empty if no such entity exists
     * @throws RepositoryException if a database access error occurs
     */
    @Override
    public Optional<Warning> delete(Long aLong) {
        try {
            Optional<Warning> entityOpt = findOne(aLong);

            if (entityOpt.isEmpty()) {
                return Optional.empty();
            }

            int affectedRows = jdbcTemplate.update("DELETE FROM warnings WHERE id = ?", aLong);

            return affectedRows > 0 ? entityOpt : Optional.empty();
        } catch (DataAccessException e) {
            throw new RepositoryException("WarningRepository: Failed to delete report", e);
        }
    }

    @Override
    public Optional<Warning> update(Warning entity) {
        return Optional.empty();
    }
}
