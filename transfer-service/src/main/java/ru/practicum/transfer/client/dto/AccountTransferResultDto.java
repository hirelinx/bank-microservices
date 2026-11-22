package ru.practicum.transfer.client.dto;

import java.math.BigDecimal;

public record AccountTransferResultDto(String fromLogin, BigDecimal fromBalance, String toLogin, String toName) {
}
