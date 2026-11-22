package ru.practicum.cash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private static final String REGISTRATION_ID = "keycloak";

    @Value("${bank.services.notification-url}")
    private String notificationBaseUrl;
    @Value("${bank.services.account-url}")
    private String accountBaseUrl;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Менеджер работает вне HTTP-запроса и получает токен сервиса по Client Credentials Flow.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        );
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build());
        return manager;
    }

    @Bean
    public RestClient notificationRestClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        return builder.clone()
                .baseUrl(notificationBaseUrl)
                .requestInterceptor(serviceTokenInterceptor(authorizedClientManager))
                .build();
    }

    @Bean
    public RestClient accountRestClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        return builder.clone()
                .baseUrl(accountBaseUrl)
                .requestInterceptor(serviceTokenInterceptor(authorizedClientManager))
                .build();
    }

    private OAuth2ClientHttpRequestInterceptor serviceTokenInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        var principal = UsernamePasswordAuthenticationToken.authenticated(applicationName, null, AuthorityUtils.NO_AUTHORITIES);
        var interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        // В исходящих запросах всегда сервисный токен, а не токен пользователя, пришедший из Gateway
        interceptor.setClientRegistrationIdResolver(request -> REGISTRATION_ID);
        interceptor.setPrincipalResolver(request -> principal);
        return interceptor;
    }
}
