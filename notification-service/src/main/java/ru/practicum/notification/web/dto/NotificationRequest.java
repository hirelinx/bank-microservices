package ru.practicum.notification.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotBlank(message = "Не указан получатель уведомления")
        String login,

        @NotBlank(message = "Не указан текст уведомления")
        @Size(max = 1024, message = "Текст уведомления не должен превышать 1024 символа")
        String message
) {
}
