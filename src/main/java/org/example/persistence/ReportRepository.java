package org.example.persistence;

import org.example.domain.Report;
import org.example.domain.Warning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Component
public class ReportRepository implements IReportRepository {

    private final DataSource dataSource;

    @Autowired
    public ReportRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Report> findOne(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Iterable<Report> getAll() {
        return null;
    }

    public Optional<Report> add(Report entity) {
        String sqlWithTimestamp = "INSERT INTO reports (user_id, start_lat, start_lng, end_lat, end_lng, created_at) VALUES (?,?,?,?,?,?)";
        String sqlWithoutTimestamp = "INSERT INTO reports (user_id, start_lat, start_lng, end_lat, end_lng) VALUES (?,?,?,?,?)";
        String sql = entity.getCreated_at() != null ? sqlWithTimestamp : sqlWithoutTimestamp; sql += " RETURNING id, created_at";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);

            preparedStatement.setLong(1, entity.getUser_id());
            preparedStatement.setDouble(2, entity.getStart_lat());
            preparedStatement.setDouble(3, entity.getStart_lng());
            preparedStatement.setDouble(4, entity.getEnd_lat());
            preparedStatement.setDouble(5, entity.getEnd_lng());
            if (entity.getCreated_at() != null) {
                preparedStatement.setLong(6, entity.getCreated_at());
            }

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                entity.setId(rs.getLong("id"));
                entity.setCreated_at(rs.getLong("created_at"));
                return Optional.of(entity);
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RepositoryException("ReportRepository: " + e);
        }
    }


    @Override
    public Optional<Report> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Report> update(Report entity) {
        return Optional.empty();
    }


    @Override
    public List<Report> getAllOfUser(Long userId) {
        String sql = "SELECT \n" +
                "    R.id AS report_id, R.start_lat, R.start_lng, R.end_lat, R.end_lng, R.created_at, R.user_id,\n" +
                "    W.id AS warning_id, W.text, W.lat, W.lng, W.created_at, W.report_id\n" +
                "FROM reports R\n" +
                "LEFT JOIN warnings W ON R.id = W.report_id\n" +
                "WHERE R.user_id = ?\n" +
                "ORDER BY R.created_at DESC";

        Map<Long, Report> reportMap = new LinkedHashMap<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {

            preparedStatement.setLong(1, userId);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                long reportId = rs.getLong("report_id");

                // If we haven’t seen this report yet, create and store it
                Report report = reportMap.get(reportId);
                if (report == null) {
                    report = new Report();
                    report.setId(reportId);
                    report.setUser_id(rs.getLong("user_id"));
                    report.setStart_lat(rs.getDouble("start_lat"));
                    report.setStart_lng(rs.getDouble("start_lng"));
                    report.setEnd_lat(rs.getDouble("end_lat"));
                    report.setEnd_lng(rs.getDouble("end_lng"));
                    report.setCreated_at(rs.getLong("created_at"));
                    report.setWarnings(new ArrayList<>()); // initialize warning list
                    reportMap.put(reportId, report);
                }

                // If there's a warning, add it
                long warningId = rs.getLong("warning_id");
                if (!rs.wasNull()) {
                    Warning warning = new Warning();
                    warning.setId(warningId);
                    warning.setText(rs.getString("text"));
                    warning.setLat(rs.getDouble("lat"));
                    warning.setLng(rs.getDouble("lng"));
                    warning.setCreated_at(rs.getLong("created_at"));
                    warning.setReport_id(reportId);
                    report.getWarnings().add(warning);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error DB "+e);
            throw new RepositoryException("WarningRepository: " + e);
        }
        return new ArrayList<>(reportMap.values());
    }
}
