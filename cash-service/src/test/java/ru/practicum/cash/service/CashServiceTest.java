package ru.practicum.cash.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.practicum.cash.client.AccountClient;
import ru.practicum.cash.client.dto.BalanceDto;
import ru.practicum.cash.exception.RemoteOperationRejectedException;
import ru.practicum.cash.model.CashAction;
import ru.practicum.cash.web.dto.CashResultDto;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {
    @Mock
    private AccountClient accountClient;
    @Mock
    private CashOperationRecorder recorder;
    @InjectMocks
    private CashService cashService;

    @Test
    void depositRecordsSuccessWithNewBalance() {
        when(accountClient.deposit("ivanov", BigDecimal.TEN)).thenReturn(new BalanceDto("ivanov", new BigDecimal("110.00")));

        CashResultDto result = cashService.deposit("ivanov", BigDecimal.TEN);

        assertThat(result).isEqualTo(new CashResultDto("ivanov", CashAction.DEPOSIT, BigDecimal.TEN, new BigDecimal("110.00")));
        verify(recorder).recordSuccess("ivanov", CashAction.DEPOSIT, BigDecimal.TEN, new BigDecimal("110.00"));
    }

    @Test
    void rejectedWithdrawalIsRecordedAndRethrown() {
        RemoteOperationRejectedException rejection =
                new RemoteOperationRejectedException(HttpStatus.CONFLICT, List.of("Недостаточно средств на счету"));
        when(accountClient.withdraw("ivanov", BigDecimal.TEN)).thenThrow(rejection);

        assertThatThrownBy(() -> cashService.withdraw("ivanov", BigDecimal.TEN)).isSameAs(rejection);

        verify(recorder).recordRejection("ivanov", CashAction.WITHDRAW, BigDecimal.TEN, "Недостаточно средств на счету");
        verify(recorder, never()).recordSuccess(anyString(), any(), any(), any());
    }
}
