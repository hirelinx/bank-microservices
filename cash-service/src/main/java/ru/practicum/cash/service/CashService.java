package ru.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.cash.client.AccountClient;
import ru.practicum.cash.client.dto.BalanceDto;
import ru.practicum.cash.exception.RemoteServiceException;
import ru.practicum.cash.model.CashAction;
import ru.practicum.cash.web.dto.CashResultDto;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CashService {
    private final AccountClient accountClient;
    private final CashOperationRecorder recorder;

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
            recorder.recordRejection(login, action, amount, e.getMessage());
            throw e;
        }

        recorder.recordSuccess(login, action, amount, balance.balance());
        return new CashResultDto(login, action, amount, balance.balance());
    }
}
