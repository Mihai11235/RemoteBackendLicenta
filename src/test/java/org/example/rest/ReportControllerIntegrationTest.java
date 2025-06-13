package org.example.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.Report;
import org.example.domain.User;
import org.example.domain.Warning;
import org.example.persistence.IReportRepository;
import org.example.persistence.IUserRepository;
import org.example.persistence.Repository;
import org.example.utils.JwtService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = start.StartRestServices.class)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // We are now using the REAL repositories, no @MockBean
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IReportRepository reportRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Report report;
    private String jwt;

    @BeforeEach
    public void setUp() {
        //clear database
        for(User u: userRepository.getAll()){
            userRepository.delete(u.getId());
        }
        //setup
        String hashedPassword = BCrypt.hashpw("testpass", BCrypt.gensalt());
        user = new User("testuser", hashedPassword, "Test User");
        user = userRepository.add(user).orElseThrow();

        for(Report r: reportRepository.getAllOfUser(user.getId())){
            reportRepository.delete(r.getId());
        }
        //setup
        report = new Report(user.getId(), 45.0, 25.0, 46.0, 26.0);

        jwt = jwtService.generateToken(user);
    }

    @Test
    void testCreate_warningIsInvalid_thenTransactionIsRolledBack() throws Exception {

        // This warning has an invalid latitude, which will cause WarningValidator to fail
        Warning invalidWarning = new Warning(null, "", 200.0, 25.6, System.currentTimeMillis());
        report.setWarnings(List.of(invalidWarning));

        // Perform the HTTP request. The controller will call the validator, which will throw
        // a ValidationException. The @Transactional(rollbackFor=...) will catch this and
        // trigger a rollback. The controller's catch block will return a 400 status.
        mockMvc.perform(post("/reports/create")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("Invalid lat/lng coordinates!")))
                .andExpect(jsonPath("$.error", Matchers.containsString("Text cannot be empty!")));


        // Check the database to prove the rollback worked. The parent Report should not have been saved.
        List<Report> reportsInDb = reportRepository.getAllOfUser(user.getId());

        assertThat(reportsInDb).isEmpty();
    }

    @Test
    void testCreate_reportIsInvalid() throws Exception {

        report = new Report(user.getId(), 200.0, 25.0, 46.0, 26.0);

        // Perform the HTTP request. The controller will call the validator, which will throw
        // a ValidationException. The @Transactional(rollbackFor=...) will catch this and
        // trigger a rollback. The controller's catch block will return a 400 status.
        mockMvc.perform(post("/reports/create")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("Invalid lat/lng coordinates!")));

        // Check the database to prove the rollback worked. The parent Report should not have been saved.
        List<Report> reportsInDb = reportRepository.getAllOfUser(user.getId());

        assertThat(reportsInDb).isEmpty();
    }

    @Test
    void testCreate_reportIsValidWarningIsNull() throws Exception {

        report = new Report(user.getId(), 23.4, 25.0, 46.0, 26.0);

        // Perform the HTTP request. The controller will call the validator, which will throw
        // a ValidationException. The @Transactional(rollbackFor=...) will catch this and
        // trigger a rollback. The controller's catch block will return a 400 status.
        mockMvc.perform(post("/reports/create")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", Matchers.containsString("Warnings cannot be null!")));

        // Check the database to prove the rollback worked. The parent Report should not have been saved.
        List<Report> reportsInDb = reportRepository.getAllOfUser(user.getId());

        assertThat(reportsInDb).isEmpty();
    }

    @Test
    void testCreate_reportAndWarningsValid() throws Exception {

        // This warning has an invalid latitude, which will cause WarningValidator to fail
        Warning validWarning = new Warning(null, "This warning is valid", 33.2, 25.6, System.currentTimeMillis());
        report.setWarnings(List.of(validWarning));

        // Perform the HTTP request. The controller will call the validator, which will throw
        // a ValidationException. The @Transactional(rollbackFor=...) will catch this and
        // trigger a rollback. The controller's catch block will return a 400 status.
        mockMvc.perform(post("/reports/create")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.start_lat").value("45.0"));

        // Check the database to prove the rollback worked. The parent Report should not have been saved.
        List<Report> reportsInDb = reportRepository.getAllOfUser(user.getId());

        assertThat(reportsInDb).hasSize(1);
    }

    @Test
    void testCreate_Unauthorized_WhenNoJwtAttribute() throws Exception {
        mockMvc.perform(post("/reports/create"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", Matchers.containsString("Missing or invalid token!")));
    }

    @Test
    void testCreate_Unauthorized_WhenTokenInvalid() throws Exception {
        mockMvc.perform(post("/reports/create")
                        .header("Authorization", "Bearer " + jwt + "invalid string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", Matchers.containsString("Invalid or expired token")));
    }


    @Test
    void testGetAll_reportAndWarningsValid() throws Exception {

        List<Report> reportsInDb = reportRepository.getAllOfUser(user.getId());
        assertThat(reportsInDb).isEmpty();

        // This warning has an invalid latitude, which will cause WarningValidator to fail
        Warning validWarning = new Warning(null, "This warning is valid", 33.2, 25.6, System.currentTimeMillis());
        report.setWarnings(List.of(validWarning));

        // Perform the HTTP request. The controller will call the validator, which will throw
        // a ValidationException. The @Transactional(rollbackFor=...) will catch this and
        // trigger a rollback. The controller's catch block will return a 400 status.
        mockMvc.perform(post("/reports/create")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(report)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.start_lat").value("45.0"));

        // Check the database to prove the rollback worked. The parent Report should not have been saved.
        reportsInDb = reportRepository.getAllOfUser(user.getId());

        assertThat(reportsInDb).hasSize(1);
    }


    @Test
    void testGetAll_Unauthorized_WhenNoJwtAttribute() throws Exception {
        mockMvc.perform(post("/reports/getAll"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", Matchers.containsString("Missing or invalid token!")));
    }


    @Test
    void testGetAll_Unauthorized_WhenTokenInvalid() throws Exception {
        mockMvc.perform(post("/reports/getAll")
                        .header("Authorization", "Bearer " + jwt + "invalid string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", Matchers.containsString("Invalid or expired token")));

    }
}