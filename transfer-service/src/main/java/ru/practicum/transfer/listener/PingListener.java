package ru.practicum.transfer.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@Slf4j
public class PingListener {
    private final RestClient notificationRestClient;
    private final RestClient accountRestClient;

    public PingListener(@Qualifier("accountRestClient") RestClient accountRestClient,
                        @Qualifier("notificationRestClient") RestClient notificationRestClient) {
        this.accountRestClient = accountRestClient;
        this.notificationRestClient = notificationRestClient;
    }

    @EventListener
    public void onStartUp(ApplicationReadyEvent event) {
        ping(accountRestClient);
        ping(notificationRestClient);
    }

    private void ping(RestClient client) {
        ResponseEntity<String> response = client.get()
                .uri("/actuator/health")
                .retrieve()
                .toEntity(String.class);

        log.info("PingListener received ping http status: {}, response: {}", response.getStatusCode(), response);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("PingListener received ping http status: " + response.getStatusCode());
        }
    }
}
