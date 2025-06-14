package org.example.domain.validators;

import org.example.business.exception.ValidationException;
import org.example.domain.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WarningValidatorTest {

    private final WarningValidator validator = WarningValidator.getInstance();
    private Warning warning;

    @BeforeEach
    public void setUp() {
        warning = new Warning();
        warning.setLat(45.0);
        warning.setLng(25.0);
        warning.setText("Valid warning text");
    }

    @Test
    public void testValidateWithValidWarning() {
        // A valid warning should not throw an exception
        assertDoesNotThrow(() -> validator.validate(warning));
    }

    @Test
    public void testValidateWithInvalidLatitude() {
        warning.setLat(-91.0); // Invalid latitude
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(warning);
        });
        assertTrue(exception.getMessage().contains("Invalid lat/lng coordinates!"));
    }

    @Test
    public void testValidateWithInvalidLongitude() {
        warning.setLng(181.0); // Invalid longitude
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(warning);
        });
        assertTrue(exception.getMessage().contains("Invalid lat/lng coordinates!"));
    }

    @Test
    public void testValidateWithNullText() {
        warning.setText(null); // Null text
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(warning);
        });
        assertTrue(exception.getMessage().contains("Text cannot be empty!"));
    }

    @Test
    public void testValidateWithEmptyText() {
        warning.setText(""); // Empty text
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(warning);
        });
        assertTrue(exception.getMessage().contains("Text cannot be empty!"));
    }

    @Test
    public void testValidateWithMultipleErrors() {
        warning.setLat(100.0); // Invalid latitude
        warning.setText("");   // Empty text

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(warning);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("Invalid lat/lng coordinates!"));
        assertTrue(message.contains("Text cannot be empty!"));
    }
}