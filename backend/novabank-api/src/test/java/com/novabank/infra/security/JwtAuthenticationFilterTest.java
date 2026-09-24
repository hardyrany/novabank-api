package com.novabank.infra.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testEmail;
    private String testPassword;
    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        seedAdminIfMissing();

        testEmail = "test" + System.currentTimeMillis() + "@novabank.com";
        testPassword = "test123";

        String adminLoginJson = """
                {
                    "email": "admin@novabank.local",
                    "password": "TestAdminPass123!"
                }
                """;

        String adminLoginResponse = mockMvc
                .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginJson))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String adminToken = JsonPath.read(adminLoginResponse, "$.token");

        String userJson = """
                {
                    "email": "%s",
                    "password": "%s",
                    "roles": ["USER"]
                }
                """.formatted(testEmail, testPassword);

        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(userJson))
                .andExpect(status().isCreated());

        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(testEmail, testPassword);

        String loginResponse = mockMvc
                .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        validToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    private void seedAdminIfMissing() {
        userRepository.findByEmail("admin@novabank.local").ifPresentOrElse(admin -> {
            admin.setPassword(passwordEncoder.encode("TestAdminPass123!"));
            admin.setRoles(Set.of(Role.ADMIN));
            admin.setIsActive(true);
            userRepository.save(admin);
        }, () -> {
            User admin = new User();
            admin.setEmail("admin@novabank.local");
            admin.setPassword(passwordEncoder.encode("TestAdminPass123!"));
            admin.setRoles(Set.of(Role.ADMIN));
            admin.setIsActive(true);
            userRepository.save(admin);
        });
    }

    @Test
    void protectedEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_ShouldReturn200_WhenValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_ShouldReturn401_WhenInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1//users/me").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpoint_ShouldReturn200_WhenNoToken() throws Exception {
        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(testEmail, testPassword);

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
