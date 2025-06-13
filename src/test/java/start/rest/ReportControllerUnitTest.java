package start.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.Report;
import org.example.domain.User;
import org.example.domain.Warning;
import org.example.persistence.IReportRepository;
import org.example.persistence.IWarningRepository;
import org.example.persistence.RepositoryException;
import org.example.rest.ReportController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Use this to enable Mockito annotations
@ExtendWith(MockitoExtension.class)
public class ReportControllerUnitTest {

    // @Mock creates a fake version of this repository
    @Mock
    private IReportRepository mockReportRepository;

    @Mock
    private IWarningRepository mockWarningRepository;

    @Mock
    private HttpServletRequest mockRequest; // We also mock the HTTP request

    // @InjectMocks creates an instance of ReportController and injects the mocks into it
    @InjectMocks
    private ReportController reportController;

    private Report report;
    private Warning goodWarning;
    private Warning badWarning;

    @BeforeEach
    void setUp() {
        // --- ARRANGE ---
        // Prepare the test data
        report = new Report();
        report.setUser_id(1L);
        report.setStart_lat(45.0);
        report.setStart_lng(25.0);
        report.setEnd_lat(46.0);
        report.setEnd_lng(26.0);

        goodWarning = new Warning(null, "This is a valid warning.", 45.5, 25.5, System.currentTimeMillis());
        badWarning = new Warning(null, "This warning will fail.", 45.6, 25.6, System.currentTimeMillis()); // Invalid lat
        report.setWarnings(List.of(goodWarning, badWarning));
    }

    @Test
    void create_shouldReturnBadRequest_whenWarningSaveFails() {
        // 1. When the controller gets the user ID from the request, return 1L.
        when(mockRequest.getAttribute("user_id")).thenReturn(1L);

        // 2. When the controller tries to save the report, pretend it works.
        when(mockReportRepository.add(any(Report.class))).thenAnswer(invocation -> {
            Report r = invocation.getArgument(0);
            r.setId(100L); // Give it a fake ID
            return Optional.of(r);
        });

        // 3. When the controller tries to save the first (good) warning, pretend it works.
        when(mockWarningRepository.add(goodWarning)).thenAnswer(invocation -> {
            Warning w = invocation.getArgument(0);
            w.setId(1001L); // Give it a fake ID
            return Optional.of(w);
        });

        // 4. When the controller tries to save the second (bad) warning, throw an error.
        when(mockWarningRepository.add(badWarning)).thenThrow(new RepositoryException("Simulated DB failure"));


        // --- ACT ---
        // Call the controller method directly
        ResponseEntity<?> response = reportController.create(report, mockRequest);


        // --- ASSERT ---
        // Check that the controller returned a 400 Bad Request status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}