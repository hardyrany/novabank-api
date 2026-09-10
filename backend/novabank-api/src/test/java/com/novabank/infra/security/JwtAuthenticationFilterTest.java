package com.novabank.infra.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testEmail;
    private String testPassword;
    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        testEmail = "test" + System.currentTimeMillis() + "@novabank.com";
        testPassword = "test123";

        String userJson = """
                {
                    "email": "%s",
                    "password": "%s",
                    "roles": ["USER"]
                }
                """.formatted(testEmail, testPassword);

        mockMvc.perform(
                post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(userJson))
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

    @Test
    void protectedEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_ShouldReturn200_WhenValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/1").header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_ShouldReturn401_WhenInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/1").header("Authorization", "Bearer token-invalido"))
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
