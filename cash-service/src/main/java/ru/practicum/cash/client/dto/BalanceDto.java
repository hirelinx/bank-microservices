package ru.practicum.cash.client.dto;

import java.math.BigDecimal;

public record BalanceDto(String login, BigDecimal balance) {
}
