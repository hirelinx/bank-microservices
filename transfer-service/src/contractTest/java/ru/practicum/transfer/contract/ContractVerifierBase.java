package ru.practicum.transfer.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.transfer.exception.RemoteOperationRejectedException;
import ru.practicum.transfer.security.config.SecurityConfig;
import ru.practicum.transfer.service.TransferService;
import ru.practicum.transfer.web.controller.TransferController;
import ru.practicum.transfer.web.dto.TransferResultDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Базовый класс для тестов, сгенерированных Spring Cloud Contract из src/contractTest/resources/contracts.
 * JWT из заголовка разбирает mock {@link JwtDecoder}.
 */
@WebMvcTest(TransferController.class)
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
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        when(jwtDecoder.decode("user-token")).thenReturn(Jwt.withTokenValue("user-token")
                .header("alg", "none")
                .claim("preferred_username", "ivanov")
                .claim("realm_access", Map.of("roles", List.of("ROLE_USER", "ACCESS_TRANSFER")))
                .build());

        when(transferService.transfer(eq("ivanov"), eq("petrov"), any())).thenAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(2);
            if (amount.compareTo(BALANCE) > 0) {
                throw new RemoteOperationRejectedException(HttpStatus.CONFLICT, List.of("Недостаточно средств на счету"));
            }
            return new TransferResultDto("ivanov", "petrov", "Петров Петр", amount, BALANCE.subtract(amount));
        });

        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
