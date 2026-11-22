package ru.practicum.transfer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.practicum.transfer.client.AccountClient;
import ru.practicum.transfer.client.dto.AccountTransferResultDto;
import ru.practicum.transfer.exception.RemoteOperationRejectedException;
import ru.practicum.transfer.exception.SelfTransferException;
import ru.practicum.transfer.web.dto.TransferResultDto;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    private AccountClient accountClient;
    @Mock
    private TransferRecorder recorder;
    @InjectMocks
    private TransferService transferService;

    @Test
    void successfulTransferIsRecordedWithRecipientName() {
        when(accountClient.transfer("ivanov", "petrov", BigDecimal.TEN))
                .thenReturn(new AccountTransferResultDto("ivanov", new BigDecimal("90.00"), "petrov", "Петров Петр"));

        TransferResultDto result = transferService.transfer("ivanov", "petrov", BigDecimal.TEN);

        assertThat(result).isEqualTo(new TransferResultDto("ivanov", "petrov", "Петров Петр", BigDecimal.TEN, new BigDecimal("90.00")));
        verify(recorder).recordSuccess("ivanov", "petrov", "Петров Петр", BigDecimal.TEN, new BigDecimal("90.00"));
    }

    @Test
    void transferToSelfIsRejectedWithoutCallingAccounts() {
        assertThatThrownBy(() -> transferService.transfer("ivanov", "ivanov", BigDecimal.TEN))
                .isInstanceOf(SelfTransferException.class);

        verifyNoInteractions(accountClient);
        verify(recorder).recordRejection("ivanov", "ivanov", BigDecimal.TEN, "Нельзя перевести деньги на свой же счёт");
    }

    @Test
    void rejectionFromAccountsIsRecordedAndRethrown() {
        RemoteOperationRejectedException rejection =
                new RemoteOperationRejectedException(HttpStatus.CONFLICT, List.of("Недостаточно средств на счету"));
        when(accountClient.transfer("ivanov", "petrov", BigDecimal.TEN)).thenThrow(rejection);

        assertThatThrownBy(() -> transferService.transfer("ivanov", "petrov", BigDecimal.TEN)).isSameAs(rejection);

        verify(recorder).recordRejection("ivanov", "petrov", BigDecimal.TEN, "Недостаточно средств на счету");
        verify(recorder, never()).recordSuccess(anyString(), anyString(), anyString(), any(), any());
    }
}
