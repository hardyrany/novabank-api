package com.novabank.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MVPIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullMVPPaymentFlow_ShouldSucceed() throws Exception {

        String uniqueDocument = "DOC-" + System.currentTimeMillis();

        String customerJson = """
                {
                    "firstName": "João",
                    "middleName": "Pedro",
                    "lastName": "Silva",
                    "email": "joao.silva.%d@email.com",
                    "phone": "+351912345678",
                    "documentNumber": "%s",
                    "documentType": "NATIONAL_ID",
                    "birthDate": "1990-01-01",
                    "address": "Rua Principal 123, Praia, Cabo Verde"
                }
                """.formatted(System.currentTimeMillis(), uniqueDocument);

        String customerResponse = mockMvc
                .perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
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
                .perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson))
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
                .contentType(MediaType.APPLICATION_JSON).content(depositJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(1500.00));

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
                .perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(account2Json))
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

        mockMvc.perform(post("/api/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content(transferJson)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/accounts/%d/balance".formatted(accountId)))
                .andExpect(status().isOk()).andExpect(content().string("1300.00"));

        mockMvc.perform(get("/api/v1/accounts/%d/balance".formatted(account2Id)))
                .andExpect(status().isOk()).andExpect(content().string("200.00"));

        mockMvc.perform(get("/api/v1/accounts/%d/transactions".formatted(accountId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].transactionType").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$[1].transactionType").value("DEPOSIT"));

        mockMvc.perform(get("/api/v1/accounts/%d/transactions".formatted(account2Id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
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
