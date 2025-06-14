package org.example.business;

import org.example.business.exception.InvalidCredentialsException;
import org.example.business.exception.UserAlreadyExistsException;
import org.example.domain.User;
import org.example.persistence.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = start.StartRestServices.class)
@Transactional // Rolls back the transaction after each test, keeping tests isolated
@ActiveProfiles("test") // Use the application-test.properties
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@EnableJpaRepositories
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private IUserRepository userRepository; // Inject the real repository to verify results

    @Test
    void create_shouldSaveUserToDatabase_whenUsernameIsNew() {
        // --- ARRANGE ---
        User newUser = new User("newuser", "password123", "New User");

        // --- ACT ---
        User createdUser = userService.create(newUser);

        // --- ASSERT ---
        // Verify the result from the service
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getPassword()).isNull(); // Password should be cleared

        // Verify directly against the database
        User userFromDb = userRepository.findOne(createdUser.getId()).orElse(null);
        assertThat(userFromDb).isNotNull();
        assertThat(userFromDb.getUsername()).isEqualTo("newuser");
        assertThat(userFromDb.getPassword()).isNotEqualTo("password123"); // Assert password is hashed
    }

    @Test
    void create_shouldThrowUserAlreadyExistsException_whenUsernameExists() {
        // --- ARRANGE ---
        // First, manually save a user to the database to create the conflict
        userService.create(new User("existinguser", "pass", "Existing User"));

        // Now, prepare a new user object with the same username
        User conflictingUser = new User("existinguser", "pass2", "Conflicting User");

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> userService.create(conflictingUser))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void login_shouldThrowInvalidCredentials_forWrongPassword() {
        // --- ARRANGE ---
        userService.create(new User("testuser", "correct_password", "Test User"));
        User loginAttempt = new User("testuser", "wrong_password", "Test User");

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> userService.login(loginAttempt))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}