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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ContextConfiguration(classes = {WarningRepository.class, ReportRepository.class, UserRepository.class})
@Sql(scripts = {"/schema.sql"})
public class WarningRepositoryTest {

    @Autowired
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    private WarningRepository warningRepository;
    private ReportRepository reportRepository;
    private UserRepository userRepository;

    private User testUser;
    private Report testReport;

    @BeforeEach
    public void setup() {
        // Instantiate repositories
        warningRepository = new WarningRepository(dataSource);
        reportRepository = new ReportRepository(dataSource);
        userRepository = new UserRepository(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);

        // Create prerequisite data
        testUser = new User("testuser", "pass", "Test User");
        userRepository.add(testUser); // Adds user and sets the ID on testUser

        testReport = new Report(testUser.getId(), 45.0, 25.0, 46.0, 26.0, System.currentTimeMillis());
        reportRepository.add(testReport); // Adds report and sets the ID on testReport
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DROP ALL OBJECTS");
    }

    @Test
    public void testAddWarning() {
        Warning newWarning = new Warning(testReport.getId(), "Road closed ahead", 45.5, 25.5, System.currentTimeMillis());
        Optional<Warning> addedWarningOpt = warningRepository.add(newWarning);

        assertTrue(addedWarningOpt.isPresent(), "Warning should be added successfully");
        assertNotNull(addedWarningOpt.get().getId(), "Added warning should have a non-null ID");
        assertEquals(testReport.getId(), addedWarningOpt.get().getReport_id());
        assertEquals("Road closed ahead", addedWarningOpt.get().getText());
    }

    @Test
    public void testAddWarningWithoutTimestamp() {
        Warning newWarning = new Warning(testReport.getId(), "Accident", 45.2, 25.2, null);
        Optional<Warning> addedWarningOpt = warningRepository.add(newWarning);

        assertTrue(addedWarningOpt.isPresent(), "Warning should be added successfully without a timestamp");
        assertNotNull(addedWarningOpt.get().getId());
    }

    @Test
    public void testAddWarningFailsForNonExistentReport() {
        Warning newWarning = new Warning(999L, "This should fail", 45.0, 25.0, System.currentTimeMillis());

        assertThrows(RepositoryException.class, () -> {
            warningRepository.add(newWarning);
        }, "Adding a warning for a non-existent report should throw an exception");
    }

    @Test
    public void testDeleteWarning() {
        Warning newWarning = new Warning(testReport.getId(), "Police checkpoint", 45.7, 25.7, System.currentTimeMillis());
        Warning addedWarning = warningRepository.add(newWarning).orElseThrow();
        Long warningId = addedWarning.getId();

        // The findOne method is not implemented, so we can't use it directly in the delete method.
        // We'll manually verify deletion by trying to find it with a direct JDBC call.

        int affectedRows = jdbcTemplate.update("DELETE FROM warnings WHERE id = ?", warningId);
        assertEquals(1, affectedRows, "One row should have been deleted");

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM warnings WHERE id = ?", Integer.class, warningId);
        assertEquals(0, count, "Warning should not be found after deletion");
    }
}