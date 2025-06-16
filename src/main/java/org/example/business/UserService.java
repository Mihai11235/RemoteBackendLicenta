package org.example.business;
import org.example.business.exception.*;
import org.example.domain.User;
import org.example.domain.validators.UserValidator;
import org.example.domain.validators.Validator;
import org.example.persistence.IUserRepository;
import org.example.persistence.RepositoryException;
import org.example.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * Service layer for user-related operations such as registration, login, and retrieval.
 * Handles validation, hashing, and interaction with the user repository.
 */
@Service
public class UserService {
    private static final Validator<User> userValidator = UserValidator.getInstance();

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Registers a new user after validation and password hashing.
     *
     * @param user The user to be created.
     * @return The created user with password set to null.
     * @throws UserAlreadyExistsException If the username is already taken.
     * @throws ValidationException If user input is invalid.
     * @throws DataAccessException If user creation fails due to repository issues.
     */
    public User create(User user) {
        userValidator.validate(user);

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        if(userRepository.findOneByUsername(user.getUsername()).isPresent()){
            throw new UserAlreadyExistsException("UserService: Username already exists!\n");
        }

        try {
            User createdUser = userRepository.add(user)
                    .orElseThrow(() -> new DataAccessException("UserService: User could not be created!\n", null));

            createdUser.setPassword(null);
            return createdUser;
        }
        catch(RepositoryException exception) {
            throw new DataAccessException("UserService: Error while creating user!\n", exception);
        }
    }

    /**
     * Authenticates a user and returns a JWT if successful.
     *
     * @param user The user credentials for login.
     * @return A JWT token on successful authentication.
     * @throws ValidationException If credentials are missing.
     * @throws InvalidCredentialsException If login fails.
     * @throws DataAccessException If repository access fails.
     */
    public String login(User user) {
        if (user.getPassword() == null || user.getUsername() == null || user.getPassword().isEmpty()) {
            throw new ValidationException("UserService: Missing username or password!\n");
        }

        try {
            User loginAttempt = userRepository.findOneByUsername(user.getUsername())
                    .orElseThrow(() -> new InvalidCredentialsException("Login failed! Incorrect username or password!\n"));

            if (!BCrypt.checkpw(user.getPassword(), loginAttempt.getPassword())) {
                throw new InvalidCredentialsException("Login failed! Incorrect username or password!\n");
            }

            return jwtService.generateToken(loginAttempt);
        }
        catch (RepositoryException e) {
            throw new DataAccessException("UserService: Error while fetching user for login!\n", e);
        }
    }

    /**
     * Retrieves the currently authenticated user by username.
     *
     * @param username The username extracted from the token.
     * @return The corresponding user with password set to null.
     * @throws InvalidCredentialsException If username is null.
     * @throws ResourceNotFoundException If user does not exist.
     * @throws DataAccessException If repository access fails.
     */
    public User getCurrentUser(String username) {
        if (username == null) {
            throw new InvalidCredentialsException("UserService: Missing or invalid token!!\n");
        }

        try {
            User user = userRepository.findOneByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("UserService: User not found!\n"));

            user.setPassword(null);

            return user;
        } catch (RepositoryException e) {
            throw new DataAccessException("UserService: Error while fetching user!\n", e);
        }
    }
}
