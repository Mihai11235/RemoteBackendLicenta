package org.example.business;

import org.example.business.exception.DataAccessException;
import org.example.business.exception.ValidationException;
import org.example.domain.Report;
import org.example.domain.Warning;
import org.example.domain.validators.ReportValidator;
import org.example.domain.validators.WarningValidator;
import org.example.persistence.IReportRepository;
import org.example.persistence.IWarningRepository;
import org.example.persistence.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceUnitTest {

    @Mock
    private IReportRepository mockReportRepository;
    @Mock
    private IWarningRepository mockWarningRepository;

    @InjectMocks
    private ReportService reportService;

    @Spy
    private final ReportValidator reportValidator = ReportValidator.getInstance();
    @Spy
    private final WarningValidator warningValidator = WarningValidator.getInstance();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reportService, "reportValidator", reportValidator);
        ReflectionTestUtils.setField(reportService, "warningValidator", warningValidator);
    }

    private Report createFullyValidReport() {
        Report report = new Report();
        report.setUser_id(1L);
        report.setStart_lat(45.0);
        report.setStart_lng(25.0);
        report.setEnd_lat(46.0);
        report.setEnd_lng(26.0);
        Warning warning = new Warning(null, "A valid warning", 45.1, 25.1, null);
        report.setWarnings(List.of(warning));
        return report;
    }

    @Test
    void create_shouldThrowDataAccessException_whenReportSaveFails() {
        // --- ARRANGE ---
        Report validReport = createFullyValidReport();
        when(mockReportRepository.add(any(Report.class))).thenThrow(new RepositoryException("DB connection failed"));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> reportService.create(validReport))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Error while creating report!");
    }

    @Test
    void create_shouldThrowDataAccessException_whenWarningSaveFails() {
        // --- ARRANGE ---
        Report validReport = createFullyValidReport();
        when(mockReportRepository.add(any(Report.class))).thenAnswer(invocation -> {
            Report r = invocation.getArgument(0);
            r.setId(100L);
            return Optional.of(r);
        });
        when(mockWarningRepository.add(any(Warning.class))).thenThrow(new RepositoryException("DB connection failed for warning"));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> reportService.create(validReport))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Error while creating report!");
    }

    @Test
    void create_shouldThrowValidationException_forInvalidReport() {
        // --- ARRANGE ---
        Report invalidReport = new Report(); // Intentionally incomplete
        invalidReport.setUser_id(1L);
        invalidReport.setWarnings(List.of()); // Fails "No warnings found!" validation

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> reportService.create(invalidReport))
                .isInstanceOf(ValidationException.class);
    }
}