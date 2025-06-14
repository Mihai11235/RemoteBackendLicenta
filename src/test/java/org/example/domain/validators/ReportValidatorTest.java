package org.example.domain.validators;

import org.example.business.exception.ValidationException;
import org.example.domain.Report;
import org.example.domain.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ReportValidatorTest {

    private final ReportValidator validator = ReportValidator.getInstance();
    private Report report;

    @BeforeEach
    public void setUp() {
        report = new Report();
        report.setStart_lat(45.0);
        report.setStart_lng(25.0);
        report.setEnd_lat(46.0);
        report.setEnd_lng(26.0);
        ArrayList<Warning> warnings = new ArrayList<>();
        warnings.add(new Warning());
        report.setWarnings(warnings);
    }

    @Test
    public void testValidateWithValidReport() {
        // A valid report should not throw an exception
        assertDoesNotThrow(() -> validator.validate(report));
    }

    @Test
    public void testValidateWithNullFields() {
        report.setStart_lat(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(report);
        });
        assertTrue(exception.getMessage().contains("Missing report fields."));
    }

    @Test
    public void testValidateWithInvalidLatitude() {
        report.setStart_lat(91.0); // Invalid latitude
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(report);
        });
        assertTrue(exception.getMessage().contains("Invalid lat/lng coordinates!"));
    }

    @Test
    public void testValidateWithInvalidLongitude() {
        report.setEnd_lng(-181.0); // Invalid longitude
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(report);
        });
        assertTrue(exception.getMessage().contains("Invalid lat/lng coordinates!"));
    }

    @Test
    public void testValidateWithNullWarnings() {
        report.setWarnings(null); // Null warnings list
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(report);
        });
        assertTrue(exception.getMessage().contains("Warnings cannot be null!"));
    }

    @Test
    public void testValidateWithEmptyWarnings() {
        report.setWarnings(Collections.emptyList()); // Empty warnings list
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(report);
        });
        assertTrue(exception.getMessage().contains("No warnings found!"));
    }

    @Test
    public void testValidateWithMultipleErrors() {
        report.setStart_lat(100.0); // Invalid lat
        report.setWarnings(null);   // Null warnings

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(report);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("Invalid lat/lng coordinates!"));
        assertTrue(message.contains("Warnings cannot be null!"));
    }
}