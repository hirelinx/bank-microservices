package ru.practicum.bankui.web.client;

import ru.practicum.bankui.web.dto.CashAction;
import ru.practicum.bankui.web.dto.CashResultDto;

import java.math.BigDecimal;

public interface CashClient {
    CashResultDto editCash(CashAction action, BigDecimal amount);
}
