package start.rest;

import org.example.domain.User;
import org.example.persistence.IUserRepository;
import org.example.rest.UserController;
import org.example.utils.JwtService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

//@ContextConfiguration(classes = StartRestServices.class)
@WebMvcTest(UserController.class)
//@ComponentScan(basePackages = "org.example")
//@ExtendWith(SpringExtension.class)

class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    private String jwt;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void testCreateUser_Success() throws Exception {
        User user = new User("testuserok", "testpass", "Test UserOk");

        User created = new User();
        created.setId(1L);
        created.setUsername("testuserok");

        Mockito.when(userRepository.add(any(User.class))).thenReturn(Optional.of(created));

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuserok"));
    }

    @Test
    void testCreateUser_Failure_Empty() throws Exception {
        User user = new User("testuserbad", "testpass", "Test UserBad");

        Mockito.when(userRepository.add(any(User.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("User could not be created!")));
    }

    @Test
    void testCreateUser_Failure_Empty_Attributes() throws Exception {
        User user = new User("", "", "Test UserBad");

        Mockito.when(userRepository.add(any(User.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("Username must be alphanumeric and start with a letter!")));
    }

    @Test
    void testCreateUser_Failure_Existing() throws Exception {
        User user = new User("existing", "testpass", "Test UserBad");

        Mockito.when(userRepository.add(any(User.class))).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findOneByUsername(any(String.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("Username already exists!")));
    }


    @Test
    void testLogin_Success() throws Exception {
        String hashedPassword = BCrypt.hashpw("testpass", BCrypt.gensalt());
        User user = new User("testuser", hashedPassword, "Test Name");

        Mockito.when(userRepository.findOneByUsername(eq("testuser"))).thenReturn(Optional.of(user));

        User loginAttempt = new User("testuser", "testpass", "Test Name");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLogin_Failure_NotFound() throws Exception {
        User loginAttempt = new User("testuser", "testpass", "Test Name");
        Mockito.when(userRepository.findOneByUsername(eq("testuser"))).thenReturn(Optional.empty());


        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", Matchers.containsString("Login failed! Incorrect username or password!")));
    }

    @Test
    void testLogin_Failure_Empty() throws Exception {
        User loginAttempt = new User("", "", "Test Name");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("Missing username or password")));
    }
}
