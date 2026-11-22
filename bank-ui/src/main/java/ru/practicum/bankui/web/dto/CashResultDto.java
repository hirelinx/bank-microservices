package ru.practicum.bankui.web.dto;

import java.math.BigDecimal;

public record CashResultDto(String login, String action, BigDecimal amount, BigDecimal balance) {
}
