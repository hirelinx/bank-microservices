package ru.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.transfer.model.OperationStatus;
import ru.practicum.transfer.model.OutboxNotification;
import ru.practicum.transfer.model.Transfer;
import ru.practicum.transfer.repository.OutboxNotificationRepository;
import ru.practicum.transfer.repository.TransferRepository;

import java.math.BigDecimal;

/**
 * Журнал переводов. Успешный перевод и уведомления обоим участникам сохраняются в одной транзакции (Transactional Outbox).
 */
@Component
@RequiredArgsConstructor
public class TransferRecorder {
    private final TransferRepository transferRepository;
    private final OutboxNotificationRepository outboxRepository;

    @Transactional
    public void recordSuccess(String fromLogin, String toLogin, String toName, BigDecimal amount, BigDecimal fromBalance) {
        transferRepository.save(new Transfer(fromLogin, toLogin, amount, OperationStatus.SUCCESS, null));

        String sum = amount.toPlainString();
        outboxRepository.save(new OutboxNotification(fromLogin, "Переведено %s руб. клиенту %s. Баланс: %s руб."
                .formatted(sum, toName, fromBalance.toPlainString())));
        outboxRepository.save(new OutboxNotification(toLogin, "Получен перевод %s руб. от клиента %s"
                .formatted(sum, fromLogin)));
    }

    @Transactional
    public void recordRejection(String fromLogin, String toLogin, BigDecimal amount, String reason) {
        transferRepository.save(new Transfer(fromLogin, toLogin, amount, OperationStatus.REJECTED, reason));
    }
}
