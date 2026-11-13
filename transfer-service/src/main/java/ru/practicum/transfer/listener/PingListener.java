package ru.practicum.transfer.listener;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Проверяет доступность зависимостей после старта. Недоступность зависимости не мешает работе приложения:
 * проверка повторяется с экспоненциальной задержкой (Resilience4j Retry), а итог только логируется.
 */
@Slf4j
@Component
public class PingListener {
    private static final Duration MAX_INTERVAL = Duration.ofSeconds(30);

    private final Map<String, RestClient> dependencies;
    private final RetryRegistry retryRegistry;

    public PingListener(@Qualifier("notificationRestClient") RestClient notificationRestClient,
                        @Qualifier("accountRestClient") RestClient accountRestClient,
                        @Value("${bank.ping.max-attempts:10}") int maxAttempts,
                        @Value("${bank.ping.initial-interval:2s}") Duration initialInterval) {
        this.dependencies = Map.of(
                "notification-service", notificationRestClient,
                "account-service", accountRestClient
        );
        this.retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(initialInterval, 2, MAX_INTERVAL))
                .build());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartUp() {
        dependencies.forEach((name, client) ->
                Thread.ofVirtual().name("ping-" + name).start(() -> ping(name, client)));
    }

    private void ping(String name, RestClient client) {
        Retry retry = retryRegistry.retry(name);
        retry.getEventPublisher().onRetry(event -> log.warn("{} недоступен (попытка {}): {}",
                name, event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));
        try {
            Retry.decorateRunnable(retry, () -> checkHealth(client)).run();
            log.info("{} доступен", name);
        } catch (RuntimeException e) {
            log.error("{} недоступен после {} попыток: {}", name, retry.getRetryConfig().getMaxAttempts(), e.getMessage());
        }
    }

    private void checkHealth(RestClient client) {
        client.get()
                .uri("/actuator/health")
                .retrieve()
                .toBodilessEntity();
    }
}
