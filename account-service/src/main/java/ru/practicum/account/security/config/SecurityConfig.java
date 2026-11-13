package ru.practicum.account.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import ru.practicum.account.security.converter.KeycloakRealmRoleConverter;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;


@Configuration
public class SecurityConfig {
    private static final String ACCESS_ACCOUNTS = "ACCESS_ACCOUNTS";

    @Bean
    public SecurityFilterChain accountSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/actuator/**").permitAll();
            auth.requestMatchers("/account/internal/**").access(allOf(hasRole("SERVICE"), hasAuthority(ACCESS_ACCOUNTS)));
            auth.requestMatchers("/account/**").access(allOf(hasRole("USER"), hasAuthority(ACCESS_ACCOUNTS)));
            auth.anyRequest().denyAll();
        });

        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );

        // Возвращаем 403 с текстом ошибки из исключения
        http.exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write(
                            accessDeniedException.getMessage() != null
                                    ? accessDeniedException.getMessage()
                                    : "Доступ запрещён"
                    );
                })
        );

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
