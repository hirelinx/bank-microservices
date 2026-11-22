package ru.practicum.account.web.dto;

/**
 * Публичные данные чужого аккаунта: без даты рождения и суммы на счёте.
 */
public record AccountShortDto(String login, String name) {
}
