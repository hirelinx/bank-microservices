package ru.practicum.account.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.account.exception.InsufficientFundsException;
import ru.practicum.account.security.config.SecurityConfig;
import ru.practicum.account.service.AccountService;
import ru.practicum.account.web.controller.AccountController;
import ru.practicum.account.web.controller.InternalAccountController;
import ru.practicum.account.web.dto.AccountDto;
import ru.practicum.account.web.dto.AccountShortDto;
import ru.practicum.account.web.dto.BalanceDto;
import ru.practicum.account.web.dto.TransferResultDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Базовый класс для тестов, сгенерированных Spring Cloud Contract из src/contractTest/resources/contracts.
 * Проверяется API контроллеров вместе с валидацией и настройками безопасности;
 * JWT из заголовка разбирает mock {@link JwtDecoder}.
 */
@WebMvcTest({AccountController.class, InternalAccountController.class})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
public abstract class ContractVerifierBase {
    private static final BigDecimal BALANCE = new BigDecimal("1000.00");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        when(jwtDecoder.decode("user-token")).thenReturn(jwt("user-token", Map.of(
                "preferred_username", "ivanov",
                "realm_access", Map.of("roles", List.of("ROLE_USER", "ACCESS_ACCOUNTS"))
        )));
        when(jwtDecoder.decode("service-token")).thenReturn(jwt("service-token", Map.of(
                "preferred_username", "service-account-cash-service",
                "realm_access", Map.of("roles", List.of("ROLE_SERVICE", "ACCESS_ACCOUNTS", "ACCESS_NOTIFICATIONS"))
        )));

        when(accountService.getOrCreate(eq("ivanov"), any()))
                .thenReturn(new AccountDto("ivanov", "Иванов Иван", LocalDate.of(2001, 1, 1), BALANCE));
        when(accountService.update(eq("ivanov"), any(), any(), any())).thenAnswer(invocation ->
                new AccountDto("ivanov", invocation.getArgument(2), invocation.getArgument(3), BALANCE));
        when(accountService.getOthers("ivanov")).thenReturn(List.of(
                new AccountShortDto("petrov", "Петров Петр"),
                new AccountShortDto("sidorov", "Сидоров Сидор")));

        when(accountService.deposit(eq("ivanov"), any())).thenAnswer(invocation ->
                new BalanceDto("ivanov", BALANCE.add(invocation.getArgument(1))));
        when(accountService.withdraw(eq("ivanov"), any())).thenAnswer(invocation ->
                new BalanceDto("ivanov", debit(invocation.getArgument(1))));
        when(accountService.transfer(eq("ivanov"), eq("petrov"), any())).thenAnswer(invocation ->
                new TransferResultDto("ivanov", debit(invocation.getArgument(2)), "petrov", "Петров Петр"));

        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    private static BigDecimal debit(BigDecimal amount) {
        if (amount.compareTo(BALANCE) > 0) {
            throw new InsufficientFundsException();
        }
        return BALANCE.subtract(amount);
    }

    private static Jwt jwt(String token, Map<String, Object> claims) {
        return Jwt.withTokenValue(token).header("alg", "none").claims(c -> c.putAll(claims)).build();
    }
}
