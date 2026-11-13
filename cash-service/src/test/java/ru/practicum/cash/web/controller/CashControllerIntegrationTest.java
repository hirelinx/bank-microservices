package ru.practicum.cash.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.cash.client.dto.BalanceDto;
import ru.practicum.cash.exception.RemoteOperationRejectedException;
import ru.practicum.cash.support.AbstractIntegrationTest;
import ru.practicum.cash.support.TestTokens;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CashControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void depositUsesLoginFromTokenAndStoresOperationWithNotification() throws Exception {
        String login = uniqueLogin();
        when(accountClient.deposit(eq(login), any())).thenReturn(new BalanceDto(login, new BigDecimal("100.00")));

        mockMvc.perform(post("/cash/deposit").with(TestTokens.user(login))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("DEPOSIT"))
                .andExpect(jsonPath("$.balance").value(100.00));

        assertThat(jdbcTemplate.queryForObject(
                "select status from cash.cash_operations where login = ?", String.class, login)).isEqualTo("SUCCESS");
        assertThat(jdbcTemplate.queryForObject(
                "select message from cash.notification_outbox where login = ? and sent_at is null", String.class, login))
                .isEqualTo("Счёт пополнен на 100 руб. Баланс: 100.00 руб.");
    }

    @Test
    void insufficientFundsAreReturnedToUserAndJournaled() throws Exception {
        String login = uniqueLogin();
        when(accountClient.withdraw(eq(login), any())).thenThrow(
                new RemoteOperationRejectedException(HttpStatus.CONFLICT, List.of("Недостаточно средств на счету")));

        mockMvc.perform(post("/cash/withdraw").with(TestTokens.user(login))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 5000}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("Недостаточно средств на счету"));

        assertThat(jdbcTemplate.queryForObject(
                "select status from cash.cash_operations where login = ?", String.class, login)).isEqualTo("REJECTED");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from cash.notification_outbox where login = ?", Integer.class, login)).isZero();
    }

    @Test
    void rejectsMissingAmount() throws Exception {
        mockMvc.perform(post("/cash/deposit").with(TestTokens.user(uniqueLogin()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Сумма должна быть заполнена"));
        verifyNoInteractions(accountClient);
    }

    @Test
    void userWithoutCashPrivilegeIsForbidden() throws Exception {
        mockMvc.perform(post("/cash/deposit").with(TestTokens.user(uniqueLogin(), "ROLE_USER", "ACCESS_ACCOUNTS"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void serviceTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/cash/deposit").with(TestTokens.user("service-account-transfer-service",
                                "ROLE_SERVICE", "ACCESS_ACCOUNTS", "ACCESS_NOTIFICATIONS"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 1}"))
                .andExpect(status().isForbidden());
    }

    private String uniqueLogin() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
