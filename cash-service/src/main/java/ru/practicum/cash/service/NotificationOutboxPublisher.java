package ru.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.cash.client.NotificationClient;
import ru.practicum.cash.model.OutboxNotification;
import ru.practicum.cash.repository.OutboxNotificationRepository;

/**
 * Доставляет уведомления из outbox в Notifications (at-least-once).
 */
@Component
@RequiredArgsConstructor
public class NotificationOutboxPublisher {
    private final OutboxNotificationRepository outboxRepository;
    private final NotificationClient notificationClient;

    @Value("${bank.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${bank.outbox.publish-delay:PT5S}")
    @Transactional
    public void publishPending() {
        for (OutboxNotification notification : outboxRepository.findPendingForUpdate(PageRequest.of(0, batchSize))) {
            if (!notificationClient.send(notification.getLogin(), notification.getMessage())) {
                // Сервис недоступен — оставшиеся уведомления будут отправлены в следующий запуск
                return;
            }
            notification.markSent();
        }
    }
}
