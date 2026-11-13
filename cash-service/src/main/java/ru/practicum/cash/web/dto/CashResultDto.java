package ru.practicum.cash.web.dto;

import ru.practicum.cash.model.CashAction;

import java.math.BigDecimal;

public record CashResultDto(String login, CashAction action, BigDecimal amount, BigDecimal balance) {
}
