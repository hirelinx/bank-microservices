package ru.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.transfer.client.AccountClient;
import ru.practicum.transfer.client.NotificationClient;
import ru.practicum.transfer.client.dto.AccountTransferResultDto;
import ru.practicum.transfer.exception.RemoteServiceException;
import ru.practicum.transfer.model.OperationStatus;
import ru.practicum.transfer.model.Transfer;
import ru.practicum.transfer.repository.TransferRepository;
import ru.practicum.transfer.web.dto.TransferResultDto;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final TransferRepository transferRepository;

    public TransferResultDto transfer(String fromLogin, String toLogin, BigDecimal amount) {
        if (fromLogin.equals(toLogin)) {
            throw reject(fromLogin, toLogin, amount,
                    new RemoteServiceException(HttpStatus.BAD_REQUEST, List.of("Нельзя перевести деньги на свой же счёт")));
        }

        AccountTransferResultDto result;
        try {
            result = accountClient.transfer(fromLogin, toLogin, amount);
        } catch (RemoteServiceException e) {
            throw reject(fromLogin, toLogin, amount, e);
        }

        transferRepository.save(new Transfer(fromLogin, toLogin, amount, OperationStatus.SUCCESS, null));

        String sum = amount.toPlainString();
        notificationClient.notify(fromLogin, "Переведено %s руб. клиенту %s. Баланс: %s руб."
                .formatted(sum, result.toName(), result.fromBalance().toPlainString()));
        notificationClient.notify(toLogin, "Получен перевод %s руб. от клиента %s".formatted(sum, fromLogin));

        return new TransferResultDto(fromLogin, toLogin, result.toName(), amount, result.fromBalance());
    }

    private RemoteServiceException reject(String fromLogin, String toLogin, BigDecimal amount, RemoteServiceException e) {
        transferRepository.save(new Transfer(fromLogin, toLogin, amount, OperationStatus.REJECTED, e.getMessage()));
        return e;
    }
}
