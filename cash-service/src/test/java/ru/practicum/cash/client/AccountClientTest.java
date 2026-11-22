package ru.practicum.cash.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.practicum.cash.config.ResilienceConfig;
import ru.practicum.cash.exception.RemoteOperationRejectedException;
import ru.practicum.cash.exception.RemoteServiceException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Клиент с боевой конфигурацией Circuit Breaker из {@link ResilienceConfig}.
 */
class AccountClientTest {
    private static final String WITHDRAW_URL = "http://account-service/account/internal/ivanov/withdraw";

    private MockRestServiceServer server;
    private AccountClient accountClient;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://account-service");
        server = MockRestServiceServer.bindTo(builder).build();

        Resilience4JConfigurationProperties properties = new Resilience4JConfigurationProperties();
        properties.setDisableTimeLimiter(true);
        circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        Resilience4JCircuitBreakerFactory factory = new Resilience4JCircuitBreakerFactory(
                circuitBreakerRegistry, TimeLimiterRegistry.ofDefaults(), null, properties);
        new ResilienceConfig().defaultCircuitBreakerCustomizer().customize(factory);

        accountClient = new AccountClient(builder.build(), factory);
    }

    @Test
    void returnsNewBalance() {
        server.expect(requestTo(WITHDRAW_URL))
                .andRespond(withSuccess("""
                        {"login": "ivanov", "balance": 90.00}
                        """, MediaType.APPLICATION_JSON));

        assertThat(accountClient.withdraw("ivanov", BigDecimal.TEN).balance()).isEqualByComparingTo("90.00");
    }

    @Test
    void businessRejectionKeepsErrorsAndDoesNotOpenCircuit() {
        server.expect(ExpectedCount.times(10), requestTo(WITHDRAW_URL))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body("""
                        {"status": 409, "detail": "Недостаточно средств на счету", "errors": ["Недостаточно средств на счету"]}
                        """));

        for (int i = 0; i < 10; i++) {
            assertThatThrownBy(() -> accountClient.withdraw("ivanov", BigDecimal.TEN))
                    .isInstanceOf(RemoteOperationRejectedException.class)
                    .extracting("errors").asList().containsExactly("Недостаточно средств на счету");
        }
        assertThat(circuitBreaker().getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void repeatedFailuresOpenCircuitAndStopCallingService() {
        server.expect(ExpectedCount.times(5), method(HttpMethod.POST))
                .andRespond(withServerError());

        for (int i = 0; i < 6; i++) {
            assertThatThrownBy(() -> accountClient.withdraw("ivanov", BigDecimal.TEN))
                    .isExactlyInstanceOf(RemoteServiceException.class)
                    .hasMessageContaining("временно недоступен");
        }

        assertThat(circuitBreaker().getState()).isEqualTo(CircuitBreaker.State.OPEN);
        server.verify();
    }

    private CircuitBreaker circuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("account-service");
    }
}
