package ru.practicum.cash.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
public class NotificationClient {
    private final RestClient notificationRestClient;

    public NotificationClient(@Qualifier("notificationRestClient") RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    /**
     * Недоступность сервиса уведомлений не должна ломать основную операцию, поэтому ошибки только логируются.
     */
    public void notify(String login, String message) {
        try {
            notificationRestClient.post()
                    .uri("/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("login", login, "message", message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Не удалось отправить уведомление пользователю {}: {}", login, e.getMessage());
        }
    }
}
