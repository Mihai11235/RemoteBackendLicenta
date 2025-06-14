package start.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.example.business.ReportService;
import org.example.business.exception.ValidationException;
import org.example.domain.Report;
import org.example.rest.ReportController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportControllerUnitTest {

    @Mock
    private ReportService mockReportService;

    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private ReportController reportController;

    @Test
    void create_shouldReturnCreated_whenServiceSucceeds() {
        // --- ARRANGE ---
        Report inputReport = new Report();
        Report createdReport = new Report();
        createdReport.setId(1L);

        when(mockRequest.getAttribute("user_id")).thenReturn(1L);
        when(mockReportService.create(any(Report.class))).thenReturn(createdReport);

        // --- ACT ---
        ResponseEntity<?> response = reportController.create(inputReport, mockRequest);

        // --- ASSERT ---
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(createdReport);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/reports/1"));
    }

    @Test
    void create_shouldThrowException_whenServiceFails() {
        // --- ARRANGE ---
        Report inputReport = new Report();
        when(mockRequest.getAttribute("user_id")).thenReturn(1L);
        // The service will throw an exception, which the GlobalExceptionHandler will handle.
        // The controller's job is to simply let the exception propagate.
        when(mockReportService.create(any(Report.class))).thenThrow(new ValidationException("Invalid data"));

        // --- ACT & ASSERT ---
        // We assert that calling the controller method results in the expected exception being thrown.
        assertThrows(ValidationException.class, () -> {
            reportController.create(inputReport, mockRequest);
        });
    }

    @Test
    void getAll_shouldReturnOkWithReports_whenServiceSucceeds() {
        // --- ARRANGE ---
        when(mockRequest.getAttribute("user_id")).thenReturn(1L);
        when(mockReportService.getAll(anyLong())).thenReturn(List.of(new Report(), new Report()));

        // --- ACT ---
        ResponseEntity<?> response = reportController.get_all(mockRequest);

        // --- ASSERT ---
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody()).hasSize(2);
    }
}