package org.example.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.User;
import org.example.persistence.UserRepository;
import org.example.utils.JwtService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = start.StartRestServices.class)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@EnableJpaRepositories
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User user;
    private String jwt;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        //clear database
        for(User u: userRepository.getAll()){
            userRepository.delete(u.getId());
        }

        //setup
        String hashedPassword = BCrypt.hashpw("testpass", BCrypt.gensalt());
        user = new User("testuser", hashedPassword, "Test Name");
        user = userRepository.add(user).orElseThrow();

        jwt = jwtService.generateToken(user);
    }


    @Test
    void testCreateUser_Success() throws Exception {
        User user = new User("testuserok", "testpassok", "Test UserOk");

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuserok"));
    }

    @Test
    void testCreateUser_Failure() throws Exception {
        // Pre-create a user with the same username to simulate conflict
        User existing = new User("existinguser", "testpass", "Existing User");
        userRepository.add(existing);

        // Attempt to create duplicate user
        User user = new User("existinguser", "anotherpass", "New User");

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", Matchers.containsString("Username already exists!")));
    }

    @Test
    void testCreate_Failure_MissingUsernameOrPassword() throws Exception {
        User createAttempt = new User(null, null, null);

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAttempt)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", Matchers.containsString("Name must be capitalized and must contain only letters!")))
                .andExpect(jsonPath("$.message", Matchers.containsString("Username must be alphanumeric and start with a letter!")))
                .andExpect(jsonPath("$.message", Matchers.containsString("Password cannot be null!")));
    }

    @Test
    void testCreate_Failure_EmptyUsernameOrPassword() throws Exception {
        User createAttempt = new User("", "", "");

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAttempt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", Matchers.containsString("Name must be capitalized and must contain only letters!")))
                .andExpect(jsonPath("$.message", Matchers.containsString("Username must be alphanumeric and start with a letter!")));
    }

    @Test
    void testLogin_Success() throws Exception {
        User loginAttempt = new User("testuser", "testpass", "Test Name");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLogin_Failure_Wrong_Password() throws Exception {
        User loginAttempt = new User("testuser", "wrongpass", "Test Name");


        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", Matchers.containsString("Login failed! Incorrect username or password!")));

    }

    @Test
    void testLogin_Failure_Nonexistent_User() throws Exception {
        User loginAttempt = new User("nonexistent", "nonexistent", "");


        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", Matchers.containsString("Login failed! Incorrect username or password!")));
    }

    @Test
    void testLogin_Failure_Empty_Fields() throws Exception {
        User loginAttempt = new User(null, null,"");


        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", Matchers.containsString("Missing username or password!")));
    }

    @Test
    void testGetCurrentUser_Success() throws Exception {
        String jwt = jwtService.generateToken(user);

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }


    @Test
    void testGetCurrentUser_Unauthorized_WhenNoJwtAttribute() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", Matchers.containsString("Missing or invalid token!")));
    }

    @Test
    void testGetCurrentUser_Unauthorized_WhenTokenInvalid() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + jwt + "invalid string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", Matchers.containsString("Invalid or expired token!")));
    }
}
