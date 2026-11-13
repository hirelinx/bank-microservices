package ru.practicum.account.security.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthenticationConverterTest {
    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

    @Test
    void usesLoginAsNameAndRealmRolesAsAuthorities() {
        Jwt jwt = jwt(Map.of(
                "sub", "8f3b6a1e",
                "preferred_username", "ivanov",
                "realm_access", Map.of("roles", List.of("ROLE_USER", "ACCESS_ACCOUNTS"))
        ));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getName()).isEqualTo("ivanov");
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER", "ACCESS_ACCOUNTS");
    }

    @Test
    void grantsNoRolesWithoutRealmAccessClaim() {
        Jwt jwt = jwt(Map.of("preferred_username", "ivanov"));

        // Spring Security сам добавляет FACTOR_BEARER — фактор аутентификации, а не роль
        assertThat(converter.convert(jwt).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .noneMatch(authority -> authority.startsWith("ROLE_") || authority.startsWith("ACCESS_"));
    }

    private Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token").header("alg", "none").claims(c -> c.putAll(claims)).build();
    }
}
