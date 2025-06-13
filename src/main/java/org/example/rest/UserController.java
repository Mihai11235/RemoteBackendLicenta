package org.example.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.User;
import org.example.domain.validators.UserValidator;
import org.example.domain.validators.ValidationException;
import org.example.domain.validators.Validator;
import org.example.persistence.IUserRepository;
import org.example.persistence.ReportRepository;
import org.example.persistence.RepositoryException;
import org.example.persistence.UserRepository;
import org.example.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Validator<User> userValidator = UserValidator.getInstance();

    @Autowired
    private IUserRepository userRepository;


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody User user){
        try{
            userValidator.validate(user);

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            if(userRepository.findOneByUsername(user.getUsername()).isPresent()){
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Username already exists!\n"));
            }

            Optional<User> optionalUser = userRepository.add(user);

            if (optionalUser.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "User could not be created!\n"));
            }

            URI location = URI.create("/users/" + optionalUser.get().getId());

            optionalUser.get().setPassword(null);
            return ResponseEntity
                    .created(location)
                    .body(optionalUser.get());

        }
        catch (ValidationException | RepositoryException exception){
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody User user) {
        try{
            if (user.getPassword() == null || user.getUsername() == null || user.getPassword().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Missing username or password!\n"));
            }

            Optional<User> optionalUser = userRepository.findOneByUsername(user.getUsername());

            if (optionalUser.isEmpty() ||
                    !BCrypt.checkpw(user.getPassword(), optionalUser.get().getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Login failed! Incorrect username or password!\n"));
            }

            JwtService jwtService = new JwtService();
            String token = jwtService.generateToken(optionalUser.get());
            return ResponseEntity.ok(Map.of("token", token));
        }
        catch (RepositoryException exception){
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try{
            String username = (String) request.getAttribute("username");

            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid token!\n"));
            }

            Optional<User> optionalUser = userRepository.findOneByUsername(username);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found!\n"));
            }

            User user = optionalUser.get();
            user.setPassword(null);

            return ResponseEntity.ok(user);
        }
        catch (Exception e){
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
