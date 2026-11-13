package ru.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.cash.model.CashAction;
import ru.practicum.cash.model.CashOperation;
import ru.practicum.cash.model.OperationStatus;
import ru.practicum.cash.model.OutboxNotification;
import ru.practicum.cash.repository.CashOperationRepository;
import ru.practicum.cash.repository.OutboxNotificationRepository;

import java.math.BigDecimal;

/**
 * Журнал операций с наличными. Успешная операция и уведомление о ней сохраняются в одной транзакции (Transactional Outbox).
 */
@Component
@RequiredArgsConstructor
public class CashOperationRecorder {
    private final CashOperationRepository cashOperationRepository;
    private final OutboxNotificationRepository outboxRepository;

    @Transactional
    public void recordSuccess(String login, CashAction action, BigDecimal amount, BigDecimal balance) {
        cashOperationRepository.save(new CashOperation(login, action, amount, OperationStatus.SUCCESS, null));
        outboxRepository.save(new OutboxNotification(login, message(action, amount, balance)));
    }

    @Transactional
    public void recordRejection(String login, CashAction action, BigDecimal amount, String reason) {
        cashOperationRepository.save(new CashOperation(login, action, amount, OperationStatus.REJECTED, reason));
    }

    private String message(CashAction action, BigDecimal amount, BigDecimal balance) {
        String template = action == CashAction.DEPOSIT
                ? "Счёт пополнен на %s руб. Баланс: %s руб."
                : "Со счёта снято %s руб. Баланс: %s руб.";
        return template.formatted(amount.toPlainString(), balance.toPlainString());
    }
}
