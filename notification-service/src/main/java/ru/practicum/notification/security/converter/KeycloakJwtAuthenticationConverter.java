package ru.practicum.notification.security.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * JWT Keycloak → Authentication: authorities из realm-ролей, имя — логин пользователя (preferred_username).
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();

    public KeycloakJwtAuthenticationConverter() {
        delegate.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        delegate.setPrincipalClaimName("preferred_username");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return delegate.convert(jwt);
    }
}
