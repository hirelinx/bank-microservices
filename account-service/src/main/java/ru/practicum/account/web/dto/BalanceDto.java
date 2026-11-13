package ru.practicum.account.web.dto;

import java.math.BigDecimal;

public record BalanceDto(String login, BigDecimal balance) {
}
