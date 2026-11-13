package ru.practicum.notification.support;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.practicum.notification.security.converter.KeycloakJwtAuthenticationConverter;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * JWT с теми же claims, что выдаёт Keycloak, преобразованные боевым конвертером сервиса.
 */
public final class TestTokens {
    private static final KeycloakJwtAuthenticationConverter CONVERTER = new KeycloakJwtAuthenticationConverter();

    private TestTokens() {
    }

    public static RequestPostProcessor user(String login) {
        return token(Map.of(
                "preferred_username", login,
                "realm_access", Map.of("roles", List.of("ROLE_USER", "ACCESS_ACCOUNTS", "ACCESS_CASH", "ACCESS_TRANSFER"))
        ));
    }

    public static RequestPostProcessor service(String clientId, String... roles) {
        return token(Map.of(
                "preferred_username", "service-account-" + clientId,
                "azp", clientId,
                "realm_access", Map.of("roles", List.of(roles))
        ));
    }

    private static RequestPostProcessor token(Map<String, Object> claims) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .build();
        return authentication(CONVERTER.convert(jwt));
    }
}
