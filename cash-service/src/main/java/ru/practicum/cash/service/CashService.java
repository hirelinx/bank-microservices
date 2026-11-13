package ru.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.cash.client.AccountClient;
import ru.practicum.cash.client.NotificationClient;
import ru.practicum.cash.client.dto.BalanceDto;
import ru.practicum.cash.exception.RemoteServiceException;
import ru.practicum.cash.model.CashAction;
import ru.practicum.cash.model.CashOperation;
import ru.practicum.cash.model.OperationStatus;
import ru.practicum.cash.repository.CashOperationRepository;
import ru.practicum.cash.web.dto.CashResultDto;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CashService {
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final CashOperationRepository cashOperationRepository;

    public CashResultDto deposit(String login, BigDecimal amount) {
        return process(login, CashAction.DEPOSIT, amount);
    }

    public CashResultDto withdraw(String login, BigDecimal amount) {
        return process(login, CashAction.WITHDRAW, amount);
    }

    private CashResultDto process(String login, CashAction action, BigDecimal amount) {
        BalanceDto balance;
        try {
            balance = action == CashAction.DEPOSIT
                    ? accountClient.deposit(login, amount)
                    : accountClient.withdraw(login, amount);
        } catch (RemoteServiceException e) {
            cashOperationRepository.save(new CashOperation(login, action, amount, OperationStatus.REJECTED, e.getMessage()));
            throw e;
        }

        cashOperationRepository.save(new CashOperation(login, action, amount, OperationStatus.SUCCESS, null));
        notificationClient.notify(login, message(action, amount, balance.balance()));

        return new CashResultDto(login, action, amount, balance.balance());
    }

    private String message(CashAction action, BigDecimal amount, BigDecimal balance) {
        String template = action == CashAction.DEPOSIT
                ? "Счёт пополнен на %s руб. Баланс: %s руб."
                : "Со счёта снято %s руб. Баланс: %s руб.";
        return template.formatted(amount.toPlainString(), balance.toPlainString());
    }
}
