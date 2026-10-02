package com.novabank.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.enums.Role;
import com.novabank.features.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ResetPasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminEmail = "admin@novabank.local";
    private String adminPassword = "TestAdminPass123!";
    private String targetEmail;
    private String targetPassword;
    private String targetId;

    @BeforeEach
    void setUp() throws Exception {
        seedAdminIfMissing();

        long timestamp = System.currentTimeMillis();
        targetEmail = "reset-target-" + timestamp + "@novabank.local";
        targetPassword = "InitialPass123!";

        // 1. Login as seeded admin
        String adminToken = "Bearer " + login(adminEmail, adminPassword);

        // 2. Create target user (ADMIN role, so it can call GET /users/{id} later)
        String userJson = """
                {
                    "email": "%s",
                    "password": "%s",
                    "roles": ["ADMIN"]
                }
                """.formatted(targetEmail, targetPassword);

        String userResponse = mockMvc
                .perform(post("/api/v1/users").header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(userJson))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists()).andReturn()
                .getResponse().getContentAsString();

        targetId = JsonPath.read(userResponse, "$.id");
    }

    private void seedAdminIfMissing() {
        User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(Set.of(Role.ADMIN));
        admin.setIsActive(true);
        admin.setMustChangePassword(false);
        userRepository.save(admin);
    }

    private String login(String email, String password) throws Exception {
        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        String response = mockMvc
                .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists()).andReturn()
                .getResponse().getContentAsString();

        return JsonPath.read(response, "$.token");
    }
}