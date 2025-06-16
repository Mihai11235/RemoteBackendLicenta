package org.example.business;

import org.example.business.exception.ValidationException;
import org.example.domain.Report;
import org.example.domain.User;
import org.example.domain.Warning;
import org.example.persistence.IReportRepository;
import org.example.persistence.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = start.StartRestServices.class)
@Transactional
@ActiveProfiles("test")

public class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;
    @Autowired
    private UserService userService;
    @Autowired
    private IReportRepository reportRepository;
    @Autowired
    private IUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Before each test, we need a user in the database to associate reports with.
        User userToCreate = new User("reportuser", "password", "Report User");
        testUser = userService.create(userToCreate);
    }

    @Test
    void create_shouldSaveReportAndWarnings_whenDataIsValid() {
        // --- ARRANGE ---
        Report newReport = new Report();
        newReport.setUser_id(testUser.getId());
        newReport.setStart_lat(45.0);
        newReport.setStart_lng(25.0);
        newReport.setEnd_lat(46.0);
        newReport.setEnd_lng(26.0);
        Warning warning1 = new Warning(null, "Warning 1", 45.1, 25.1, null);
        Warning warning2 = new Warning(null, "Warning 2", 45.2, 25.2, null);
        newReport.setWarnings(List.of(warning1, warning2));

        // --- ACT ---
        reportService.create(newReport);

        // --- ASSERT ---
        // Verify by fetching the data back from the database
        List<Report> reportsFromDb = reportRepository.getAllOfUser(testUser.getId());
        assertThat(reportsFromDb).hasSize(1);
        Report fetchedReport = reportsFromDb.get(0);
        assertThat(fetchedReport.getStart_lat()).isEqualTo(45.0);
        assertThat(fetchedReport.getWarnings()).hasSize(2);
        assertThat(fetchedReport.getWarnings().get(0).getText()).isEqualTo("Warning 1");
    }

    @Test
    void create_shouldRollbackTransaction_whenAWarningIsInvalid() {
        // --- ARRANGE ---
        // This report is invalid because one of its warnings will fail validation (text is null)
        Report invalidReport = new Report();
        invalidReport.setUser_id(testUser.getId());
        invalidReport.setStart_lat(45.0);
        invalidReport.setStart_lng(25.0);
        invalidReport.setEnd_lat(46.0);
        invalidReport.setEnd_lng(26.0);
        Warning invalidWarning = new Warning(null, null, 45.1, 25.1, null); // Null text
        invalidReport.setWarnings(List.of(invalidWarning));

        // --- ACT & ASSERT ---
        // 1. Assert that calling create() with this invalid data throws a ValidationException
        assertThatThrownBy(() -> reportService.create(invalidReport))
                .isInstanceOf(ValidationException.class);

        // Verify that no report was saved to the database.
        // This proves that the we successfully rolled back the transaction.
        List<Report> reportsFromDb = reportRepository.getAllOfUser(testUser.getId());
        assertThat(reportsFromDb).isEmpty();
    }
}