package com.novabank.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.repository.AccountRepository;
import com.novabank.features.customer.entity.Customer;
import com.novabank.features.customer.repository.CustomerRepository;
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
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String supportToken;
    private String userToken;
    private Long ownAccountId;
    private Long otherAccountId;

    @BeforeEach
    void setUp() throws Exception {
        long timestamp = System.currentTimeMillis();

        String adminEmail = "admin" + timestamp + "@novabank.local";
        String supportEmail = "support" + timestamp + "@novabank.local";
        String userEmail = "user" + timestamp + "@novabank.local";
        String password = "TestPass123!";

        createUser(adminEmail, password, Role.ADMIN);
        createUser(supportEmail, password, Role.SUPPORT);
        createUser(userEmail, password, Role.USER);

        adminToken = "Bearer " + login(adminEmail, password);
        supportToken = "Bearer " + login(supportEmail, password);
        userToken = "Bearer " + login(userEmail, password);

        // Create customer for the USER (email matches so ownership validation passes)
        Customer userCustomer = createCustomer(userEmail, "USER-" + timestamp);
        ownAccountId = createAccount(userCustomer.getId(), "ACC-" + (timestamp % 10000000000L));

        // Create a different customer + account (not owned by USER)
        Customer otherCustomer =
                createCustomer("other" + timestamp + "@novabank.local", "OTHER-" + timestamp);
        otherAccountId = createAccount(otherCustomer.getId(), "OTH-" + (timestamp % 10000000000L));
    }

    private void createUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(role));
        user.setIsActive(true);
        userRepository.save(user);
    }

    private Customer createCustomer(String email, String documentNumber) {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail(email);
        customer.setDocumentNumber(documentNumber);
        customer.setDocumentType("NATIONAL_ID");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setActive(true);
        return customerRepository.save(customer);
    }

    private Long createAccount(Long customerId, String accountNumber) {
        Account account = new Account();
        account.setCustomerId(customerId);
        account.setAccountNumber(accountNumber);
        account.setAccountType("CHECKING");
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("USD");
        account.setActive(true);
        return accountRepository.save(account).getId();
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

    @Test
    void get_accountById_ShouldReturn200_WhenUserOwnsAccount() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + ownAccountId).header("Authorization", userToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(ownAccountId));
    }

    @Test
    void get_accountById_ShouldReturn403_WhenUserDoesNotOwnAccount() throws Exception {
        mockMvc.perform(
                get("/api/v1/accounts/" + otherAccountId).header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }
}
