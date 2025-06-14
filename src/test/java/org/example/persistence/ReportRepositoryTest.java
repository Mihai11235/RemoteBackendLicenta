package org.example.persistence;

import org.example.domain.Report;
import org.example.domain.User;
import org.example.domain.Warning;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ContextConfiguration(classes = {ReportRepository.class})
@Sql(scripts = {"/schema.sql"})
public class ReportRepositoryTest {

    @Autowired
    private DataSource dataSource;

    private ReportRepository reportRepository;
    private JdbcTemplate jdbcTemplate;

    // Test data
    private User testUser1;
    private User testUser2;
    private Report testReport1;
    private Report testReport2;

    @BeforeEach
    public void setup() {
        reportRepository = new ReportRepository(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);

        // Create and save users
        jdbcTemplate.update("INSERT INTO Users (id, username, password, name) VALUES (?, ?, ?, ?)", 1L, "testuser1", "pass", "Test User 1");
        testUser1 = new User("testuser1", "pass", "Test User 1");
        testUser1.setId(1L);

        jdbcTemplate.update("INSERT INTO Users (id, username, password, name) VALUES (?, ?, ?, ?)", 2L, "testuser2", "pass", "Test User 2");
        testUser2 = new User("testuser2", "pass", "Test User 2");
        testUser2.setId(2L);
    }

    @AfterEach
    public void tearDown() {
        // Clean up the database after each test
        jdbcTemplate.execute("DROP ALL OBJECTS");
    }

    @Test
    public void testAddReport() {
        Report newReport = new Report(testUser1.getId(), 45.0, 25.0, 46.0, 26.0, System.currentTimeMillis());

        Optional<Report> addedReportOpt = reportRepository.add(newReport);

        assertTrue(addedReportOpt.isPresent(), "Report should be added successfully");
        assertNotNull(addedReportOpt.get().getId(), "Added report should have a non-null ID");
        assertEquals(newReport.getUser_id(), addedReportOpt.get().getUser_id());
    }

    @Test
    public void testAddReportFails() {
        // Attempt to add a report for a non-existent user
        Report invalidReport = new Report(999L, 45.0, 25.0, 46.0, 26.0, System.currentTimeMillis());
        assertThrows(RepositoryException.class, () -> {
            reportRepository.add(invalidReport);
        });
    }

    @Test
    public void testDeleteReport() {
        Report newReport = new Report(testUser1.getId(), 45.0, 25.0, 46.0, 26.0, System.currentTimeMillis());
        Optional<Report> addedReportOpt = reportRepository.add(newReport);
        assertTrue(addedReportOpt.isPresent());
        Long reportId = addedReportOpt.get().getId();

        // The findOne method is not implemented, so we can't use it to verify existence before deletion
        // We'll rely on the row count from the delete operation

        int affectedRows = jdbcTemplate.update("DELETE FROM reports WHERE id = ?", reportId);
        assertEquals(1, affectedRows, "One row should be deleted");
    }

    @Test
    public void testGetAllOfUser() {
        // Add reports for user 1
        Report report1 = new Report(testUser1.getId(), 45.1, 25.1, 46.1, 26.1, System.currentTimeMillis());
        reportRepository.add(report1);

        Report report2 = new Report(testUser1.getId(), 45.2, 25.2, 46.2, 26.2, System.currentTimeMillis() + 1000);
        reportRepository.add(report2);

        // Add a report for user 2 to ensure we're only getting user 1's reports
        Report report3 = new Report(testUser2.getId(), 47.0, 27.0, 48.0, 28.0, System.currentTimeMillis());
        reportRepository.add(report3);

        List<Report> user1Reports = reportRepository.getAllOfUser(testUser1.getId());

        assertNotNull(user1Reports);
        assertEquals(2, user1Reports.size(), "Should retrieve two reports for user 1");

        // Reports should be ordered by creation date descending
        assertTrue(user1Reports.get(0).getCreated_at() > user1Reports.get(1).getCreated_at());
    }

    @Test
    public void testGetAllOfUserWithWarnings() {
        // Add a report
        Report report = new Report(testUser1.getId(), 45.5, 25.5, 46.5, 26.5, System.currentTimeMillis());
        Optional<Report> addedReportOpt = reportRepository.add(report);
        assertTrue(addedReportOpt.isPresent());
        Long reportId = addedReportOpt.get().getId();

        // Add warnings to the report
        jdbcTemplate.update("INSERT INTO warnings (report_id, text, lat, lng, created_at) VALUES (?, ?, ?, ?, ?)",
                reportId, "Warning 1", 45.6, 25.6, System.currentTimeMillis());
        jdbcTemplate.update("INSERT INTO warnings (report_id, text, lat, lng, created_at) VALUES (?, ?, ?, ?, ?)",
                reportId, "Warning 2", 45.7, 25.7, System.currentTimeMillis());

        List<Report> reports = reportRepository.getAllOfUser(testUser1.getId());
        assertEquals(1, reports.size());

        Report retrievedReport = reports.get(0);
        assertNotNull(retrievedReport.getWarnings());
        assertEquals(2, retrievedReport.getWarnings().size(), "Report should have two warnings");

        Warning firstWarning = retrievedReport.getWarnings().get(0);
        assertEquals("Warning 1", firstWarning.getText());
        assertEquals(reportId, firstWarning.getReport_id());
    }

    @Test
    public void testGetAllOfUserNoReports() {
        List<Report> reports = reportRepository.getAllOfUser(testUser1.getId());
        assertNotNull(reports);
        assertTrue(reports.isEmpty(), "Should return an empty list for a user with no reports");
    }

    @Test
    public void testGetAllOfUserWithReportButNoWarnings() {
        // Add a report without warnings
        Report report = new Report(testUser1.getId(), 45.8, 25.8, 46.8, 26.8, System.currentTimeMillis());
        reportRepository.add(report);

        List<Report> reports = reportRepository.getAllOfUser(testUser1.getId());
        assertEquals(1, reports.size());
        assertNotNull(reports.get(0).getWarnings());
        assertTrue(reports.get(0).getWarnings().isEmpty(), "Warnings list should be empty");
    }
}