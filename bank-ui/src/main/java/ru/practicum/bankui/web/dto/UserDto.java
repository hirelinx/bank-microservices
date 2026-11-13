package ru.practicum.bankui.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDto(
        String login,
        String name,
        LocalDate birthdate,
        BigDecimal balance
) {}
