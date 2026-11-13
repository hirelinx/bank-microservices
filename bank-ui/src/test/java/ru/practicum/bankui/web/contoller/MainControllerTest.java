package ru.practicum.bankui.web.contoller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.security.config.SecurityConfig;
import ru.practicum.bankui.service.AccountServiceImpl;
import ru.practicum.bankui.support.TestProfile;
import ru.practicum.bankui.web.dto.AccountDto;
import ru.practicum.bankui.web.dto.CashAction;
import ru.practicum.bankui.web.dto.UserDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MainController.class)
@Import(SecurityConfig.class)
@TestProfile
class MainControllerTest {
    private static final UserDto IVANOV =
            new UserDto("ivanov", "Иванов Иван", LocalDate.of(2001, 1, 1), new BigDecimal("1000.00"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        when(accountService.getCurrentAccount()).thenReturn(IVANOV);
        when(accountService.getOtherAccounts()).thenReturn(List.of(new AccountDto("petrov", "Петров Петр")));
    }

    @Test
    void redirectsAnonymousUserToKeycloakLogin() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/keycloak"));
    }

    @Test
    void rendersAccountPage() throws Exception {
        mockMvc.perform(get("/account").with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "Иванов Иван"))
                .andExpect(model().attribute("birthdate", "2001-01-01"))
                .andExpect(model().attribute("sum", new BigDecimal("1000.00")))
                .andExpect(model().attribute("accounts", List.of(new AccountDto("petrov", "Петров Петр"))))
                .andExpect(content().string(containsString("Петров Петр")));
    }

    @Test
    void showsValidationErrorsFromAccountsService() throws Exception {
        when(accountService.updateCurrentAccount(any(), any())).thenThrow(
                new BankServiceException(List.of("Возраст должен быть старше 18 лет")));

        mockMvc.perform(post("/account").with(oauth2Login()).with(csrf())
                        .param("name", "Иванов Иван").param("birthdate", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Возраст должен быть старше 18 лет")))
                .andExpect(content().string(containsString("Возраст должен быть старше 18 лет")));
    }

    @Test
    void showsInfoAfterSuccessfulWithdrawal() throws Exception {
        when(accountService.editCash(CashAction.GET, "100")).thenReturn("Снято 100 руб");

        mockMvc.perform(post("/cash").with(oauth2Login()).with(csrf())
                        .param("value", "100").param("action", "GET"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Снято 100 руб"));
    }

    @Test
    void showsInsufficientFundsError() throws Exception {
        when(accountService.transfer("petrov", "5000"))
                .thenThrow(new BankServiceException("Недостаточно средств на счету"));

        mockMvc.perform(post("/transfer").with(oauth2Login()).with(csrf())
                        .param("value", "5000").param("login", "petrov"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errors", contains("Недостаточно средств на счету")));
        verify(accountService).transfer("petrov", "5000");
    }

    @Test
    void rejectsFormWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/cash").with(oauth2Login()).param("value", "100").param("action", "PUT"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(accountService);
    }
}
