package com.novabank.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    void resetPassword_fullFlow_ShouldForceChangeAndRestoreAccess() throws Exception {

        // 1. Target logs in with the initial password → 200
        String initialToken = "Bearer " + login(targetEmail, targetPassword);

        // 2. Target can call GET /users/{id} (ADMIN role) → 200
        mockMvc.perform(get("/api/v1/users/" + targetId).header("Authorization", initialToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(targetEmail));

        // 3. Admin logs in and resets the target's password
        String adminToken = "Bearer " + login(adminEmail, adminPassword);

        String resetResponse = mockMvc
                .perform(post("/api/v1/users/" + targetId + "/reset-password")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.temporaryPassword").exists())
                .andReturn().getResponse().getContentAsString();

        String temporaryPassword = JsonPath.read(resetResponse, "$.temporaryPassword");

        // 4. Target logs in with the temporary password → 200
        String resetToken = "Bearer " + login(targetEmail, temporaryPassword);

        // 5. Target tries GET /users/{id} → 403 (mustChangePassword = true)
        mockMvc.perform(get("/api/v1/users/" + targetId).header("Authorization", resetToken))
                .andExpect(status().isForbidden());

        // 6. Target changes the password via /auth/change-password → 200
        String newPassword = "NewPass456!";
        String changeJson = """
                {
                    "currentPassword": "%s",
                    "newPassword": "%s",
                    "confirmPassword": "%s"
                }
                """.formatted(temporaryPassword, newPassword, newPassword);

        mockMvc.perform(post("/api/v1/auth/change-password").header("Authorization", resetToken)
                .contentType(MediaType.APPLICATION_JSON).content(changeJson))
                .andExpect(status().isOk());

        // 7. Target logs in with the new password → 200
        String finalToken = "Bearer " + login(targetEmail, newPassword);

        // 8. Target can call GET /users/{id} again → 200 (flag cleared)
        mockMvc.perform(get("/api/v1/users/" + targetId).header("Authorization", finalToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(targetEmail));
    }
}
