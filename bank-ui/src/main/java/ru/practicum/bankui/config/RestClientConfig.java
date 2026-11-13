package ru.practicum.bankui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${bank.gateway.base-url}")
    private String baseUrl;

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Подставляет access token пользователя, полученный по Authorization Code Flow (с автоматическим refresh).
     * Без вошедшего пользователя (например, health-check при старте) запрос уходит без токена.
     */
    @Bean
    public RestClient gatewayRestClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager,
            OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        OAuth2ClientHttpRequestInterceptor requestInterceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        requestInterceptor.setClientRegistrationIdResolver(request ->
                SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken token
                        ? token.getAuthorizedClientRegistrationId()
                        : null
        );
        // При 401/403 от Gateway токен удаляется из сессии, и следующий запрос отправит пользователя на повторный вход
        requestInterceptor.setAuthorizationFailureHandler(
                OAuth2ClientHttpRequestInterceptor.authorizationFailureHandler(authorizedClientRepository));

        return builder.clone()
                .baseUrl(baseUrl)
                .requestInterceptor(requestInterceptor)
                .build();
    }
}
