package ru.practicum.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.notification.model.Notification;
import ru.practicum.notification.repository.NotificationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void savesNotification() {
        notificationService.send("ivanov", "Счёт пополнен", "cash-service");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue())
                .extracting(Notification::getLogin, Notification::getMessage, Notification::getSender)
                .containsExactly("ivanov", "Счёт пополнен", "cash-service");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }
}
