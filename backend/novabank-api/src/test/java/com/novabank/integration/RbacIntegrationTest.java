package com.novabank.integration;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.jayway.jsonpath.JsonPath;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.enums.Role;
import com.novabank.features.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String supportToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        String adminEmail = "admin" + System.currentTimeMillis() + "@novabank.local";
        String supportEmail = "support" + System.currentTimeMillis() + "@novabank.local";
        String userEmail = "user" + System.currentTimeMillis() + "@novabank.local";
        String password = "TestPass123!";

        createUser(adminEmail, password, Role.ADMIN);
        createUser(supportEmail, password, Role.SUPPORT);
        createUser(userEmail, password, Role.USER);

        adminToken = "Bearer " + login(adminEmail, password);
        supportToken = "Bearer " + login(supportEmail, password);
        userToken = "Bearer " + login(userEmail, password);
    }

    private void createUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(role));
        user.setIsActive(true);
        userRepository.save(user);
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
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.token");
    }
}
