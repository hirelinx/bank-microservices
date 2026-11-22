package ru.practicum.bankui.web.client;

import ru.practicum.bankui.web.dto.TransferResultDto;

import java.math.BigDecimal;

public interface TransferClient {
    TransferResultDto transfer(String toLogin, BigDecimal amount);
}
