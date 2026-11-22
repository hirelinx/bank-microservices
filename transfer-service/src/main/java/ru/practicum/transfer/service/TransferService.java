package ru.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.transfer.client.AccountClient;
import ru.practicum.transfer.client.dto.AccountTransferResultDto;
import ru.practicum.transfer.exception.RemoteServiceException;
import ru.practicum.transfer.exception.SelfTransferException;
import ru.practicum.transfer.web.dto.TransferResultDto;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final AccountClient accountClient;
    private final TransferRecorder recorder;

    public TransferResultDto transfer(String fromLogin, String toLogin, BigDecimal amount) {
        if (fromLogin.equals(toLogin)) {
            SelfTransferException e = new SelfTransferException();
            recorder.recordRejection(fromLogin, toLogin, amount, e.getMessage());
            throw e;
        }

        AccountTransferResultDto result;
        try {
            result = accountClient.transfer(fromLogin, toLogin, amount);
        } catch (RemoteServiceException e) {
            recorder.recordRejection(fromLogin, toLogin, amount, e.getMessage());
            throw e;
        }

        recorder.recordSuccess(fromLogin, toLogin, result.toName(), amount, result.fromBalance());
        return new TransferResultDto(fromLogin, toLogin, result.toName(), amount, result.fromBalance());
    }
}
