package ru.practicum.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.account.client.NotificationClient;
import ru.practicum.account.model.OutboxNotification;
import ru.practicum.account.repository.OutboxNotificationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxPublisherTest {
    @Mock
    private OutboxNotificationRepository outboxRepository;
    @Mock
    private NotificationClient notificationClient;
    @InjectMocks
    private NotificationOutboxPublisher publisher;

    @Test
    void marksDeliveredNotificationsAndStopsOnFirstFailure() {
        ReflectionTestUtils.setField(publisher, "batchSize", 10);
        OutboxNotification first = new OutboxNotification("ivanov", "first");
        OutboxNotification second = new OutboxNotification("ivanov", "second");
        OutboxNotification third = new OutboxNotification("ivanov", "third");
        when(outboxRepository.findPendingForUpdate(any(Pageable.class))).thenReturn(List.of(first, second, third));
        when(notificationClient.send("ivanov", "first")).thenReturn(true);
        when(notificationClient.send("ivanov", "second")).thenReturn(false);

        publisher.publishPending();

        assertThat(first.getSentAt()).isNotNull();
        assertThat(second.getSentAt()).isNull();
        assertThat(third.getSentAt()).isNull();
        verify(notificationClient, never()).send("ivanov", "third");
    }
}
