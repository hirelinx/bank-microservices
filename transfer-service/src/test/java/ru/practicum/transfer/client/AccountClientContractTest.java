package ru.practicum.transfer.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import ru.practicum.transfer.client.dto.AccountTransferResultDto;
import ru.practicum.transfer.config.ResilienceConfig;
import ru.practicum.transfer.exception.RemoteOperationRejectedException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Consumer-сторона контракта с account-service: клиент проверяется против stub-ов,
 * сгенерированных из контрактов producer-а.
 */
class AccountClientContractTest {
    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .downloadStub("ru.practicum", "account-service", "+", "stubs")
            .stubsMode(StubRunnerProperties.StubsMode.CLASSPATH);

    private AccountClient accountClient;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .baseUrl(stubRunner.findStubUrl("account-service").toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer transfer-service-token")
                .build();
        accountClient = new AccountClient(restClient, circuitBreakerFactory());
    }

    @Test
    void transfer() {
        AccountTransferResultDto result = accountClient.transfer("ivanov", "petrov", new BigDecimal("100"));

        assertThat(result.fromBalance()).isEqualByComparingTo("900.00");
        assertThat(result.toName()).isEqualTo("Петров Петр");
    }

    @Test
    void transferMoreThanBalanceIsRejected() {
        assertThatThrownBy(() -> accountClient.transfer("ivanov", "petrov", new BigDecimal("100000")))
                .isInstanceOfSatisfying(RemoteOperationRejectedException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(HttpStatus.CONFLICT.value());
                    assertThat(e.getErrors()).containsExactly("Недостаточно средств на счету");
                });
    }

    private Resilience4JCircuitBreakerFactory circuitBreakerFactory() {
        Resilience4JConfigurationProperties properties = new Resilience4JConfigurationProperties();
        properties.setDisableTimeLimiter(true);
        Resilience4JCircuitBreakerFactory factory = new Resilience4JCircuitBreakerFactory(
                CircuitBreakerRegistry.ofDefaults(), TimeLimiterRegistry.ofDefaults(), null, properties);
        new ResilienceConfig().defaultCircuitBreakerCustomizer().customize(factory);
        return factory;
    }
}
