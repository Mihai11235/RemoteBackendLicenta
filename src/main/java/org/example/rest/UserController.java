package org.example.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.example.business.UserService;
import org.example.domain.User;
import org.example.domain.validators.UserValidator;
import org.example.domain.validators.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;

/**
 * REST controller for managing user-related operations.
 * Provides endpoints for user creation, login, and fetching the current user.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Validator<User> userValidator = UserValidator.getInstance();

    @Autowired
    private UserService userService;

    /**
     * Creates a new user.
     * Validates the user data before saving it to the database.
     *
     * @param user The user to be created.
     * @return ResponseEntity with the created user and its location.
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody User user){
        User createdUser = userService.create(user);

        URI location = URI.create("/users/" + user.getId());
        return ResponseEntity
                .created(location)
                .body(createdUser);
    }

    /**
     * Logs in a user by validating credentials and generating a token.
     *
     * @param user The user credentials for login.
     * @return ResponseEntity with the generated token.
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody User user) {
        String token = userService.login(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Retrieves the current user based on the token provided in the request.
     *
     * @param request The HTTP request containing the token.
     * @return ResponseEntity with the current user details.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        User user = userService.getCurrentUser(username);
        return ResponseEntity.ok(user);
    }
}
