package ru.practicum.bankui.web.dto;

import java.math.BigDecimal;

public record TransferResultDto(String fromLogin, String toLogin, String toName, BigDecimal amount, BigDecimal balance) {
}
