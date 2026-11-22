package ru.practicum.gateway.security.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenRelayGatewayFilterFactoryTest {
    private final GatewayFilter filter = new JwtTokenRelayGatewayFilterFactory().apply(new Object());
    private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
    private final GatewayFilterChain chain = exchange -> {
        forwarded.set(exchange);
        return Mono.empty();
    };

    @Test
    void relaysTokenFromSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("user-token").header("alg", "none").claim("sub", "ivanov").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/account/me"));

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new JwtAuthenticationToken(jwt))))
                .verifyComplete();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer user-token");
    }

    @Test
    void fallsBackToIncomingAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/account/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer header-token"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer header-token");
    }

    @Test
    void failsWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/account/me"));

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(IllegalStateException.class)
                .verify();
        assertThat(forwarded.get()).isNull();
    }
}
