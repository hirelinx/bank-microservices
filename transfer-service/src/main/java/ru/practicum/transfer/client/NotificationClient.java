package ru.practicum.transfer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class NotificationClient {
    private final RestClient notificationRestClient;
    private final CircuitBreaker circuitBreaker;

    public NotificationClient(@Qualifier("notificationRestClient") RestClient notificationRestClient,
                              CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.notificationRestClient = notificationRestClient;
        this.circuitBreaker = circuitBreakerFactory.create("notification-service");
    }

    /**
     * @return true, если уведомление принято сервисом Notifications
     */
    public boolean send(String login, String message) {
        return circuitBreaker.run(() -> {
            notificationRestClient.post()
                    .uri("/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("login", login, "message", message))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        }, throwable -> {
            log.warn("Не удалось отправить уведомление пользователю {}: {}", login, throwable.getMessage());
            return false;
        });
    }
}
