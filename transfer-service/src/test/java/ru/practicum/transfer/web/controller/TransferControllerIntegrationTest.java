package ru.practicum.transfer.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.transfer.client.dto.AccountTransferResultDto;
import ru.practicum.transfer.exception.RemoteServiceException;
import ru.practicum.transfer.support.AbstractIntegrationTest;
import ru.practicum.transfer.support.TestTokens;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void transferStoresJournalAndNotificationsForBothParticipants() throws Exception {
        String sender = uniqueLogin();
        when(accountClient.transfer(eq(sender), eq("petrov"), any()))
                .thenReturn(new AccountTransferResultDto(sender, new BigDecimal("60.00"), "petrov", "Петров Петр"));

        mockMvc.perform(post("/transfer").with(TestTokens.user(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"petrov\", \"amount\": 40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toName").value("Петров Петр"))
                .andExpect(jsonPath("$.balance").value(60.00));

        assertThat(jdbcTemplate.queryForObject(
                "select status from transfer.transfers where from_login = ?", String.class, sender)).isEqualTo("SUCCESS");
        List<String> recipients = jdbcTemplate.queryForList(
                "select login from transfer.notification_outbox where message like ? order by login",
                String.class, "%" + sender + "%");
        assertThat(recipients).contains("petrov");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from transfer.notification_outbox where login = ?", Integer.class, sender)).isEqualTo(1);
    }

    @Test
    void transferToSelfIsBadRequest() throws Exception {
        String login = uniqueLogin();

        mockMvc.perform(post("/transfer").with(TestTokens.user(login))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"%s\", \"amount\": 1}".formatted(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Нельзя перевести деньги на свой же счёт"));
    }

    @Test
    void unavailableAccountsServiceIsReported() throws Exception {
        String sender = uniqueLogin();
        when(accountClient.transfer(eq(sender), any(), any())).thenThrow(RemoteServiceException.unavailable("аккаунтов"));

        mockMvc.perform(post("/transfer").with(TestTokens.user(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"petrov\", \"amount\": 1}"))
                .andExpect(status().isServiceUnavailable());

        assertThat(jdbcTemplate.queryForObject(
                "select status from transfer.transfers where from_login = ?", String.class, sender)).isEqualTo("REJECTED");
    }

    @Test
    void requiresRecipient() throws Exception {
        mockMvc.perform(post("/transfer").with(TestTokens.user(uniqueLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Выберите получателя перевода"));
    }

    @Test
    void userWithoutTransferPrivilegeIsForbidden() throws Exception {
        mockMvc.perform(post("/transfer").with(TestTokens.user(uniqueLogin(), "ROLE_USER", "ACCESS_CASH"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"petrov\", \"amount\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cashServiceTokenCannotCallTransfer() throws Exception {
        mockMvc.perform(post("/transfer").with(TestTokens.user("service-account-cash-service",
                                "ROLE_SERVICE", "ACCESS_ACCOUNTS", "ACCESS_NOTIFICATIONS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toLogin\": \"petrov\", \"amount\": 1}"))
                .andExpect(status().isForbidden());
    }

    private String uniqueLogin() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
