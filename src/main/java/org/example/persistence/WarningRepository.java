package org.example.persistence;

import org.example.domain.Warning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Component
public class WarningRepository implements IWarningRepository{

    private final DataSource dataSource;

    @Autowired
    public WarningRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Warning> findOne(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Iterable<Warning> getAll() {
        return null;
    }

    public Optional<Warning> add(Warning entity){
        String sqlWithTimestamp = "INSERT INTO warnings (report_id, text, lat, lng, created_at) OUTPUT inserted.id, inserted.created_at VALUES (?,?,?,?,?)";
        String sqlWithoutTimestamp = "INSERT INTO warnings (report_id, text, lat, lng) OUTPUT inserted.id, inserted.created_at VALUES (?,?,?,?)";
        String sql = entity.getCreated_at() != null ? sqlWithTimestamp : sqlWithoutTimestamp;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

//            connection.setAutoCommit(false);

            preparedStatement.setLong(1, entity.getReport_id());
            preparedStatement.setString(2, entity.getText());
            preparedStatement.setDouble(3, entity.getLat());
            preparedStatement.setDouble(4, entity.getLng());
            if (entity.getCreated_at() != null) {
                preparedStatement.setLong(5, entity.getCreated_at());
            }

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                entity.setId(rs.getLong("id"));
                entity.setCreated_at(rs.getLong("created_at"));
                return Optional.of(entity);
            } else {
                return Optional.empty();
            }
        } catch (NullPointerException | SQLException e){
            throw new RepositoryException("WarningRepository: " + e);
        }
    }

    @Override
    public Optional<Warning> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Warning> update(Warning entity) {
        return Optional.empty();
    }
}
