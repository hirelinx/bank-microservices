package ru.practicum.account.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer-сторона контракта с notification-service: клиент проверяется против stub-ов,
 * сгенерированных из контрактов producer-а.
 */
class NotificationClientContractTest {
    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .downloadStub("ru.practicum", "notification-service", "+", "stubs")
            .stubsMode(StubRunnerProperties.StubsMode.CLASSPATH);

    @Test
    void notificationIsAccepted() {
        RestClient restClient = RestClient.builder()
                .baseUrl(stubRunner.findStubUrl("notification-service").toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer account-service-token")
                .build();
        NotificationClient client = new NotificationClient(restClient, circuitBreakerFactory());

        assertThat(client.send("ivanov", "Данные аккаунта обновлены")).isTrue();
    }

    private Resilience4JCircuitBreakerFactory circuitBreakerFactory() {
        Resilience4JConfigurationProperties properties = new Resilience4JConfigurationProperties();
        properties.setDisableTimeLimiter(true);
        return new Resilience4JCircuitBreakerFactory(
                CircuitBreakerRegistry.ofDefaults(), TimeLimiterRegistry.ofDefaults(), null, properties);
    }
}
