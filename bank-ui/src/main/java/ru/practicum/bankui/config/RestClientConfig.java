package ru.practicum.bankui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
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

    @Bean
    public RestClient gatewayRestClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        OAuth2ClientHttpRequestInterceptor requestInterceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        return builder.clone()
                .baseUrl(baseUrl)
                .requestInterceptor(requestInterceptor)
                .build();
    }
}
