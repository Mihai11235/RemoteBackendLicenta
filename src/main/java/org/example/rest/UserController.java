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

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Validator<User> userValidator = UserValidator.getInstance();

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody User user){
        User createdUser = userService.create(user);

        URI location = URI.create("/users/" + user.getId());
        return ResponseEntity
                .created(location)
                .body(createdUser);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody User user) {
        String token = userService.login(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        User user = userService.getCurrentUser(username);
        return ResponseEntity.ok(user);
    }
}
