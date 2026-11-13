package ru.practicum.account.web.dto;

import java.math.BigDecimal;

public record TransferResultDto(String fromLogin, BigDecimal fromBalance, String toLogin, String toName) {
}
