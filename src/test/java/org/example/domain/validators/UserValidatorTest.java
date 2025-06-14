package org.example.domain.validators;

import org.example.business.exception.ValidationException;
import org.example.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidatorTest {

    private final UserValidator validator = UserValidator.getInstance();
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("validuser1", "validPassword", "Valid Name");
    }

    @Test
    public void testValidateWithValidUser() {
        // A valid user should not throw an exception
        assertDoesNotThrow(() -> validator.validate(user));
    }

    @Test
    public void testValidateWithInvalidName() {
        user.setName("invalid name"); // Not capitalized
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(user);
        });
        assertTrue(exception.getMessage().contains("Name must be capitalized and must contain only letters!"));
    }

    @Test
    public void testValidateWithInvalidNameContainingNumbers() {
        user.setName("Invalid123"); // Contains numbers
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(user);
        });
        assertTrue(exception.getMessage().contains("Name must be capitalized and must contain only letters!"));
    }

    @Test
    public void testValidateWithInvalidUsername() {
        user.setUsername("1invalid"); // Starts with a number
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(user);
        });
        assertTrue(exception.getMessage().contains("Username must be alphanumeric and start with a letter!"));
    }

    @Test
    public void testValidateWithInvalidUsernameWithSpecialChars() {
        user.setUsername("invalid-user"); // Contains special character
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(user);
        });
        assertTrue(exception.getMessage().contains("Username must be alphanumeric and start with a letter!"));
    }

    @Test
    public void testValidateWithNullPassword() {
        user.setPassword(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(user);
        });
        assertTrue(exception.getMessage().contains("Password cannot be null!"));
    }

    @Test
    public void testValidateWithMultipleErrors() {
        user.setName("invalid name");
        user.setUsername("123invalid");

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(user);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("Name must be capitalized"));
        assertTrue(message.contains("Username must be alphanumeric"));
    }
}