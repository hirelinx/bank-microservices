package ru.practicum.gateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * Маршрут /account/** направлен на локальный HTTP-сервер вместо account-service из Service Discovery.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false"
})
class GatewayRoutingIntegrationTest {
    private static final AtomicReference<String> receivedAuthorization = new AtomicReference<>();
    private static final HttpServer downstream = startDownstream();

    @Autowired
    private ApplicationContext context;

    private WebTestClient client;

    @DynamicPropertySource
    static void routeToDownstream(DynamicPropertyRegistry registry) {
        String prefix = "spring.cloud.gateway.server.webflux.routes[0].";
        registry.add(prefix + "id", () -> "account-service");
        registry.add(prefix + "uri", () -> "http://localhost:" + downstream.getAddress().getPort());
        registry.add(prefix + "predicates[0]", () -> "Path=/account/**");
        registry.add(prefix + "filters[0]", () -> "JwtTokenRelay");
    }

    @BeforeEach
    void setUp() {
        receivedAuthorization.set(null);
        client = WebTestClient.bindToApplicationContext(context).apply(springSecurity()).configureClient().build();
    }

    @AfterAll
    static void stopDownstream() {
        downstream.stop(0);
    }

    @Test
    void relaysUserTokenToService() {
        client.mutateWith(mockJwt().jwt(jwt -> jwt.tokenValue("user-access-token")))
                .get().uri("/account/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("{\"login\":\"ivanov\"}");

        assertThat(receivedAuthorization.get()).isEqualTo("Bearer user-access-token");
    }

    @Test
    void rejectsRequestWithoutToken() {
        client.get().uri("/account/me")
                .exchange()
                .expectStatus().isUnauthorized();

        assertThat(receivedAuthorization.get()).isNull();
    }

    @Test
    void healthIsPublic() {
        client.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    private static HttpServer startDownstream() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/account", exchange -> {
                receivedAuthorization.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                byte[] body = "{\"login\":\"ivanov\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
