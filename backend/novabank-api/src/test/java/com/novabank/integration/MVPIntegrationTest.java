package com.novabank.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
public class MVPIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testEmail;
    private String testPassword;
    private String bearerToken;

    @BeforeEach
    void setUp() throws Exception {
        seedAdminIfMissing();

        testEmail = "test" + System.currentTimeMillis() + "@novabank.com";
        testPassword = "test123";

        // 1. Login as seeded admin to get token
        String adminLoginJson = """
                {
                    "email": "admin@novabank.local",
                    "password": "TestAdminPass123!"
                }
                """;

        String adminLoginResponse = mockMvc
                .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists()).andReturn()
                .getResponse().getContentAsString();

        String adminToken = "Bearer " + JsonPath.read(adminLoginResponse, "$.token");

        // 2. Create test user (ADMIN-only endpoint)
        String userJson = """
                {
                    "email": "%s",
                    "password": "%s",
                    "roles": ["ADMIN"]
                }
                """.formatted(testEmail, testPassword);

        mockMvc.perform(post("/api/v1/users").header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(userJson))
                .andExpect(status().isCreated());

        // 3. Login as the new test user
        String loginJson = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(testEmail, testPassword);

        String loginResponse = mockMvc
                .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists()).andReturn()
                .getResponse().getContentAsString();

        bearerToken = "Bearer " + JsonPath.read(loginResponse, "$.token");
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
    void fullMVPPaymentFlow_ShouldSucceed() throws Exception {

        String uniqueDocument = "DOC-" + System.currentTimeMillis();

        String customerJson = """
                {
                    "firstName": "João",
                    "middleName": "Pedro",
                    "lastName": "Silva",
                    "email": "%s",
                    "phone": "+351912345678",
                    "documentNumber": "%s",
                    "documentType": "NATIONAL_ID",
                    "birthDate": "1990-01-01",
                    "address": "Rua Principal 123, Praia, Cabo Verde"
                }
                """.formatted(testEmail, uniqueDocument);

        String customerResponse = mockMvc
                .perform(post("/api/v1/customers").header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(customerJson))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists()).andReturn()
                .getResponse().getContentAsString();

        Long customerId = ((Number) JsonPath.read(customerResponse, "$.id")).longValue();

        String accountJson = """
                {
                    "customerId": %d,
                    "accountNumber": "ACC-%d",
                    "accountType": "CHECKING",
                    "initialBalance": 1000.00,
                    "currency": "USD"
                }
                """.formatted(customerId, System.currentTimeMillis());

        String accountResponse = mockMvc
                .perform(post("/api/v1/accounts").header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(accountJson))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists()).andReturn()
                .getResponse().getContentAsString();

        Long accountId = extractId(accountResponse);

        String depositJson = """
                {
                    "amount": 500.00,
                    "description": "Initial Deposit"
                }
                """;

        mockMvc.perform(post("/api/v1/accounts/%d/deposit".formatted(accountId))
                .header("Authorization", bearerToken).contentType(MediaType.APPLICATION_JSON)
                .content(depositJson)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.00));

        String account2Json = """
                {
                    "customerId": %d,
                    "accountNumber": "ACC-%d",
                    "accountType": "SAVINGS",
                    "initialBalance": 0.00,
                    "currency": "USD"
                }
                """.formatted(customerId, System.currentTimeMillis() + 1);

        String account2Response = mockMvc
                .perform(post("/api/v1/accounts").header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(account2Json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists()).andReturn()
                .getResponse().getContentAsString();

        Long account2Id = extractId(account2Response);

        String transferJson = """
                {
                    "sourceAccountId": %d,
                    "targetAccountId": %d,
                    "amount": 200.00,
                    "description": "Transfer to saving"
                }
                """.formatted(accountId, account2Id);

        mockMvc.perform(post("/api/v1/transfers").header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON).content(transferJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/accounts/%d/balance".formatted(accountId))
                .header("Authorization", bearerToken)).andExpect(status().isOk())
                .andExpect(content().string("1300.00"));

        mockMvc.perform(get("/api/v1/accounts/%d/balance".formatted(account2Id))
                .header("Authorization", bearerToken)).andExpect(status().isOk())
                .andExpect(content().string("200.00"));

        mockMvc.perform(get("/api/v1/accounts/%d/transactions".formatted(accountId))
                .header("Authorization", bearerToken)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].transactionType").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$[1].transactionType").value("DEPOSIT"));

        mockMvc.perform(get("/api/v1/accounts/%d/transactions".formatted(account2Id))
                .header("Authorization", bearerToken)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].transactionType").value("TRANSFER_IN"));
    }

    private Long extractId(String json) {
        String idField = "\"id\":";
        int startIndex = json.indexOf(idField) + idField.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }
        return Long.parseLong(json.substring(startIndex, endIndex).trim());
    }
}
