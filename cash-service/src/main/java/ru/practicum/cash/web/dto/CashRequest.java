package ru.practicum.cash.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CashRequest(
        @NotNull(message = "Сумма должна быть заполнена")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля")
        @Digits(integer = 17, fraction = 2, message = "Некорректная сумма")
        BigDecimal amount
) {
}
