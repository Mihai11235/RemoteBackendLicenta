package org.example.persistence;
import org.example.domain.Report;
import org.example.domain.Warning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Component
public class ReportRepository implements IReportRepository {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReportRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<Report> findOne(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Iterable<Report> getAll() {
        return null;
    }

    @Override
    public Optional<Report> add(Report entity) {
        try {
            String sql = "INSERT INTO reports (user_id, start_lat, start_lng, end_lat, end_lng, created_at) VALUES (?,?,?,?,?,?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, entity.getUser_id());
                ps.setDouble(2, entity.getStart_lat());
                ps.setDouble(3, entity.getStart_lng());
                ps.setDouble(4, entity.getEnd_lat());
                ps.setDouble(5, entity.getEnd_lng());
                ps.setLong(6, entity.getCreated_at());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                entity.setId(keyHolder.getKey().longValue());
                return Optional.of(entity);
            } else {
                return Optional.empty();
            }
        } catch (DataAccessException e) {
            throw new RepositoryException("ReportRepository: Failed to add report", e);
        }
    }

    @Override
    public Optional<Report> delete(Long aLong) {
        try {
            Optional<Report> entityOpt = findOne(aLong);

            if (entityOpt.isEmpty()) {
                return Optional.empty();
            }

            int affectedRows = jdbcTemplate.update("DELETE FROM reports WHERE id = ?", aLong);

            return affectedRows > 0 ? entityOpt : Optional.empty();
        } catch (DataAccessException e) {
            throw new RepositoryException("ReportRepository: Failed to delete report", e);
        }
    }

    @Override
    public Optional<Report> update(Report entity) {
        return Optional.empty();
    }


//    @Override
//    public List<Report> getAllOfUser(Long userId) {
//        String sql = "SELECT \n" +
//                "    R.id AS report_id, R.start_lat, R.start_lng, R.end_lat, R.end_lng, R.created_at as r_created_at, R.user_id,\n" +
//                "    W.id AS warning_id, W.text, W.lat, W.lng, W.created_at as w_created_at, W.report_id\n" +
//                "FROM reports R\n" +
//                "LEFT JOIN warnings W ON R.id = W.report_id\n" +
//                "WHERE R.user_id = ?\n" +
//                "ORDER BY R.created_at DESC";
//
//        Map<Long, Report> reportMap = new LinkedHashMap<>();
//
//        try (Connection connection = dataSource.getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(sql);
//        ) {
//
//            preparedStatement.setLong(1, userId);
//
//            ResultSet rs = preparedStatement.executeQuery();
//
//            while (rs.next()) {
//                long reportId = rs.getLong("report_id");
//
//                // If we haven’t seen this report yet, create and store it
//                Report report = reportMap.get(reportId);
//                if (report == null) {
//                    report = new Report();
//                    report.setId(reportId);
//                    report.setUser_id(rs.getLong("user_id"));
//                    report.setStart_lat(rs.getDouble("start_lat"));
//                    report.setStart_lng(rs.getDouble("start_lng"));
//                    report.setEnd_lat(rs.getDouble("end_lat"));
//                    report.setEnd_lng(rs.getDouble("end_lng"));
//                    report.setCreated_at(rs.getLong("r_created_at"));
//                    report.setWarnings(new ArrayList<>()); // initialize warning list
//                    reportMap.put(reportId, report);
//                }
//
//                // If there's a warning, add it
//                long warningId = rs.getLong("warning_id");
//                if (!rs.wasNull()) {
//                    Warning warning = new Warning();
//                    warning.setId(warningId);
//                    warning.setText(rs.getString("text"));
//                    warning.setLat(rs.getDouble("lat"));
//                    warning.setLng(rs.getDouble("lng"));
//                    warning.setCreated_at(rs.getLong("w_created_at"));
//                    warning.setReport_id(reportId);
//                    report.getWarnings().add(warning);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error DB "+e);
//            throw new RepositoryException("ReportRepository: Failed to get all reports of user" + e);
//        }
//        return new ArrayList<>(reportMap.values());
//    }

    @Override
    public List<Report> getAllOfUser(Long userId) {
        String sql = "SELECT \n" +
                "    R.id AS report_id, R.start_lat, R.start_lng, R.end_lat, R.end_lng, R.created_at as r_created_at, R.user_id,\n" +
                "    W.id AS warning_id, W.text, W.lat, W.lng, W.created_at as w_created_at, W.report_id\n" +
                "FROM reports R\n" +
                "LEFT JOIN warnings W ON R.id = W.report_id\n" +
                "WHERE R.user_id = ?\n" +
                "ORDER BY R.created_at DESC";

        try {
            return jdbcTemplate.query(sql, new ResultSetExtractor<List<Report>>() {
                @Override
                public List<Report> extractData(ResultSet rs) throws SQLException, DataAccessException {
                    Map<Long, Report> reportMap = new LinkedHashMap<>();
                    while (rs.next()) {
                        long reportId = rs.getLong("report_id");
                        Report report = reportMap.get(reportId);
                        if (report == null) {
                            report = new Report();
                            report.setId(reportId);
                            report.setUser_id(rs.getLong("user_id"));
                            report.setStart_lat(rs.getDouble("start_lat"));
                            report.setStart_lng(rs.getDouble("start_lng"));
                            report.setEnd_lat(rs.getDouble("end_lat"));
                            report.setEnd_lng(rs.getDouble("end_lng"));
                            report.setCreated_at(rs.getLong("r_created_at"));
                            report.setWarnings(new ArrayList<>()); // initialize warning list
                            reportMap.put(reportId, report);
                        }

                        long warningId = rs.getLong("warning_id");
                        if (!rs.wasNull()) {
                            Warning warning = new Warning();
                            warning.setId(warningId);
                            warning.setText(rs.getString("text"));
                            warning.setLat(rs.getDouble("lat"));
                            warning.setLng(rs.getDouble("lng"));
                            warning.setCreated_at(rs.getLong("w_created_at"));
                            warning.setReport_id(reportId);
                            report.getWarnings().add(warning);
                        }
                    }
                    return new ArrayList<>(reportMap.values());
                }
            }, userId);
        } catch (DataAccessException e) {
            throw new RepositoryException("ReportRepository: Failed to get all reports of user", e);
        }
    }
}
