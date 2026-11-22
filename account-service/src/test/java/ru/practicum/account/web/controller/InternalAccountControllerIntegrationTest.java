package ru.practicum.account.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.practicum.account.support.AbstractIntegrationTest;
import ru.practicum.account.support.TestTokens;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalAccountControllerIntegrationTest extends AbstractIntegrationTest {
    private static final RequestPostProcessor CASH_SERVICE =
            TestTokens.service("cash-service", "ROLE_SERVICE", "ACCESS_ACCOUNTS", "ACCESS_NOTIFICATIONS");

    private String sender;
    private String recipient;

    @BeforeEach
    void createAccounts() throws Exception {
        sender = "sender-" + UUID.randomUUID().toString().substring(0, 8);
        recipient = "recipient-" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(get("/account/me").with(TestTokens.user(sender))).andExpect(status().isOk());
        mockMvc.perform(get("/account/me").with(TestTokens.user(recipient))).andExpect(status().isOk());
    }

    @Test
    void depositAndWithdrawChangeBalance() throws Exception {
        mockMvc.perform(post("/account/internal/{login}/deposit", sender).with(CASH_SERVICE)
                        .contentType(MediaType.APPLICATION_JSON).content(amount("150.50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.50));

        mockMvc.perform(post("/account/internal/{login}/withdraw", sender).with(CASH_SERVICE)
                        .contentType(MediaType.APPLICATION_JSON).content(amount("50.50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void withdrawMoreThanBalanceReturnsConflict() throws Exception {
        mockMvc.perform(post("/account/internal/{login}/withdraw", sender).with(CASH_SERVICE)
                        .contentType(MediaType.APPLICATION_JSON).content(amount("1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("Недостаточно средств на счету"));
    }

    @Test
    void transferMovesMoneyBetweenAccounts() throws Exception {
        mockMvc.perform(post("/account/internal/{login}/deposit", sender).with(CASH_SERVICE)
                        .contentType(MediaType.APPLICATION_JSON).content(amount("100")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/account/internal/transfer")
                        .with(TestTokens.service("transfer-service", "ROLE_SERVICE", "ACCESS_ACCOUNTS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromLogin": "%s", "toLogin": "%s", "amount": 40}
                                """.formatted(sender, recipient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromBalance").value(60.00));

        mockMvc.perform(get("/account/me").with(TestTokens.user(recipient)))
                .andExpect(jsonPath("$.balance").value(40.00));
    }

    @Test
    void rejectsNonPositiveAmount() throws Exception {
        mockMvc.perform(post("/account/internal/{login}/deposit", sender).with(CASH_SERVICE)
                        .contentType(MediaType.APPLICATION_JSON).content(amount("0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Сумма должна быть больше нуля"));
    }

    @Test
    void userTokenCannotChangeBalance() throws Exception {
        mockMvc.perform(post("/account/internal/{login}/deposit", sender).with(TestTokens.user(sender))
                        .contentType(MediaType.APPLICATION_JSON).content(amount("1000000")))
                .andExpect(status().isForbidden());
    }

    @Test
    void serviceWithoutAccountsPrivilegeIsForbidden() throws Exception {
        mockMvc.perform(post("/account/internal/{login}/deposit", sender)
                        .with(TestTokens.service("account-service", "ROLE_SERVICE", "ACCESS_NOTIFICATIONS"))
                        .contentType(MediaType.APPLICATION_JSON).content(amount("1")))
                .andExpect(status().isForbidden());
    }

    private String amount(String value) {
        return "{\"amount\": " + value + "}";
    }
}
