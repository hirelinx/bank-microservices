package ru.practicum.notification.contract;

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
import ru.practicum.notification.security.config.SecurityConfig;
import ru.practicum.notification.service.NotificationService;
import ru.practicum.notification.web.controller.NotificationController;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Базовый класс для тестов, сгенерированных Spring Cloud Contract из src/contractTest/resources/contracts.
 * Проверяется API контроллера вместе с настройками безопасности; JWT из заголовка разбирает mock {@link JwtDecoder}.
 */
@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false"
})
public abstract class ContractVerifierBase {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        when(jwtDecoder.decode("service-token")).thenReturn(Jwt.withTokenValue("service-token")
                .header("alg", "none")
                .claim("preferred_username", "service-account-cash-service")
                .claim("azp", "cash-service")
                .claim("realm_access", Map.of("roles", List.of("ROLE_SERVICE", "ACCESS_NOTIFICATIONS")))
                .build());
        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
