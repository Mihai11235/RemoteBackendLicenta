package org.example.business;

import org.example.business.exception.InvalidCredentialsException;
import org.example.business.exception.UserAlreadyExistsException;
import org.example.business.exception.ValidationException;
import org.example.domain.User;
import org.example.persistence.IUserRepository;
import org.example.utils.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private IUserRepository mockUserRepository;
    @Mock
    private JwtService mockJwtService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "password123", "Test User");
    }

    @Test
    void create_shouldThrowUserAlreadyExistsException_whenUsernameIsTaken() {
        when(mockUserRepository.findOneByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void create_shouldReturnUserWithNullPassword_onSuccess() {
        when(mockUserRepository.findOneByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(mockUserRepository.add(any(User.class))).thenReturn(Optional.of(user));

        User result = userService.create(user);

        assertThat(result.getPassword()).isNull();
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void login_shouldThrowValidationException_ifUsernameIsNull() {
        user.setUsername(null);
        assertThatThrownBy(() -> userService.login(user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_ifUserNotFound() {
        when(mockUserRepository.findOneByUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(user))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Incorrect username or password");
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_ifPasswordIsWrong() {
        String hashedPassword = BCrypt.hashpw("wrong-password", BCrypt.gensalt());
        user.setPassword(hashedPassword);

        when(mockUserRepository.findOneByUsername(anyString())).thenReturn(Optional.of(user));

        User loginAttempt = new User("testuser", "password123", "Test User");

        assertThatThrownBy(() -> userService.login(loginAttempt))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Incorrect username or password");
    }

    @Test
    void login_shouldReturnToken_onSuccess() {
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        User userFromDb = new User(user.getUsername(), hashedPassword, user.getName());

        when(mockUserRepository.findOneByUsername(user.getUsername())).thenReturn(Optional.of(userFromDb));
        when(mockJwtService.generateToken(any(User.class))).thenReturn("fake.jwt.token");

        String token = userService.login(user);

        assertThat(token).isEqualTo("fake.jwt.token");
    }
}