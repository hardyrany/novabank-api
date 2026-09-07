package com.novabank.features.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.novabank.features.account.dto.AccountRequest;
import com.novabank.features.account.dto.AccountResponse;
import com.novabank.features.account.entity.Account;
import com.novabank.features.account.mapper.AccountMapper;
import com.novabank.features.account.service.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    private Account account;
    private AccountRequest accountRequest;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        account = new Account();
        account.setId(1L);
        account.setCustomerId(1L);
        account.setAccountNumber("ACC-1234567890");
        account.setAccountType("CHECKING");
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setCurrency("USD");
        account.setActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        accountRequest = new AccountRequest(1L, "ACC-1234567890", "CHECKING", BigDecimal.valueOf(1000.00), "USD");

        accountResponse = new AccountResponse(
                1L, 1L, "ACC-1234567890", "CHECKING",
                BigDecimal.valueOf(1000.00), "USD", true,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void createAccount_ShouldReturn201Created() throws Exception {
        String accountJson = """
                {
                    "customerId": 1,
                    "accountNumber": "ACC-1234567890",
                    "accountType": "CHECKING",
                    "initialBalance": 1000.00,
                    "currency": "USD"
                }
                """;

        // Mockar o Mapper para conversão de entrada
        when(accountMapper.toEntity(any(AccountRequest.class))).thenReturn(account);

        // Mockar o Service
        when(accountService.createAccount(any(Account.class))).thenReturn(account);

        // 🔥 Mockar o Mapper para conversão de saída
        when(accountMapper.toResponse(any(Account.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(accountJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("ACC-1234567890"))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }
}