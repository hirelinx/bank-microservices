package ru.practicum.account.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Не указан отправитель")
        String fromLogin,

        @NotBlank(message = "Не указан получатель")
        String toLogin,

        @NotNull(message = "Сумма должна быть заполнена")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля")
        @Digits(integer = 17, fraction = 2, message = "Некорректная сумма")
        BigDecimal amount
) {
}
