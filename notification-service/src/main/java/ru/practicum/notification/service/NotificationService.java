package ru.practicum.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.notification.model.Notification;
import ru.practicum.notification.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void send(String login, String message, String sender) {
        notificationRepository.save(new Notification(login, message, sender));
        log.info("Уведомление для '{}' от {}: {}", login, sender, message);
    }
}
