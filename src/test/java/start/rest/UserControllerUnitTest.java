package org.example.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.example.business.UserService;
import org.example.business.exception.UserAlreadyExistsException;
import org.example.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {

    @Mock
    private UserService mockUserService;
    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private UserController userController;

    @Test
    void create_shouldReturnCreated_whenServiceSucceeds() {
        User user = new User("test", "pass", "Test");
        when(mockUserService.create(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = userController.create(user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(user);
    }

    @Test
    void create_shouldThrowException_whenUsernameExists() {
        User user = new User("test", "pass", "Test");
        when(mockUserService.create(any(User.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists!"));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userController.create(user);
        });
    }

    @Test
    void login_shouldReturnOkWithToken() {
        User user = new User("test", "pass", "Test");
        String fakeToken = "abc.123.def";
        when(mockUserService.login(any(User.class))).thenReturn(fakeToken);

        ResponseEntity<?> response = userController.login(user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Map<String, String>) response.getBody()).containsEntry("token", fakeToken);
    }

    @Test
    void getCurrentUser_shouldReturnOkWithUser() {
        User user = new User("test", null, "Test");
        when(mockRequest.getAttribute("username")).thenReturn("test");
        when(mockUserService.getCurrentUser(anyString())).thenReturn(user);

        ResponseEntity<?> response = userController.getCurrentUser(mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(user);
    }
}