package ru.practicum.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.notification.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
