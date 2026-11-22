package ru.practicum.bankui.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import ru.practicum.bankui.security.logout.KeycloakLogoutSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${bank.keycloak.public-url}/realms/${bank.keycloak.realm}/protocol/openid-connect/logout") String endSessionUri,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId
    ) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/css/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(new KeycloakLogoutSuccessHandler(endSessionUri, clientId))
                );

        return http.build();
    }
}
