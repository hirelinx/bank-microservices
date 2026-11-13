package ru.practicum.cash.support;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ru.practicum.cash.security.converter.KeycloakJwtAuthenticationConverter;

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

    public static RequestPostProcessor user(String login, String... roles) {
        return token(Map.of(
                "preferred_username", login,
                "realm_access", Map.of("roles", List.of(roles))
        ));
    }

    public static RequestPostProcessor user(String login) {
        return user(login, "ROLE_USER", "ACCESS_ACCOUNTS", "ACCESS_CASH", "ACCESS_TRANSFER");
    }

    private static RequestPostProcessor token(Map<String, Object> claims) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .build();
        return authentication(CONVERTER.convert(jwt));
    }
}
