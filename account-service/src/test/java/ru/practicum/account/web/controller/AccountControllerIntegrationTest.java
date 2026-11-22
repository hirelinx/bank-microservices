package ru.practicum.account.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.account.support.AbstractIntegrationTest;
import ru.practicum.account.support.TestTokens;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsAccountForNewUserFromJwtClaims() throws Exception {
        String login = uniqueLogin();

        mockMvc.perform(get("/account/me").with(TestTokens.user(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.name").value("Тестов Тест"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void updatesAccountAndStoresNotificationInOutbox() throws Exception {
        String login = uniqueLogin();

        mockMvc.perform(put("/account/me").with(TestTokens.user(login))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Петров Пётр", "birthdate": "1990-05-15"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Петров Пётр"))
                .andExpect(jsonPath("$.birthdate").value("1990-05-15"));

        Integer pending = jdbcTemplate.queryForObject(
                "select count(*) from account.notification_outbox where login = ? and sent_at is null", Integer.class, login);
        assertThat(pending).isEqualTo(1);
    }

    @Test
    void rejectsEmptyNameAndUnderageBirthdate() throws Exception {
        mockMvc.perform(put("/account/me").with(TestTokens.user(uniqueLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "birthdate": "2020-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", containsInAnyOrder(
                        "Фамилия и имя должны быть заполнены",
                        "Возраст должен быть старше 18 лет")));
    }

    @Test
    void othersContainOnlyLoginAndNameOfOtherAccounts() throws Exception {
        mockMvc.perform(get("/account/others").with(TestTokens.user("ivanov")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].login", hasItem("petrov")))
                .andExpect(jsonPath("$[*].login", not(hasItem("ivanov"))))
                .andExpect(jsonPath("$[0].balance").doesNotExist())
                .andExpect(jsonPath("$[0].birthdate").doesNotExist());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/account/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void serviceTokenCannotReadUserEndpoints() throws Exception {
        mockMvc.perform(get("/account/me").with(TestTokens.service("cash-service", "ROLE_SERVICE", "ACCESS_ACCOUNTS")))
                .andExpect(status().isForbidden());
    }

    private String uniqueLogin() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
