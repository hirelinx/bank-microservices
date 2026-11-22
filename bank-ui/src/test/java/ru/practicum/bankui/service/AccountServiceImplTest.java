package ru.practicum.bankui.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.web.client.AccountClient;
import ru.practicum.bankui.web.client.CashClient;
import ru.practicum.bankui.web.client.TransferClient;
import ru.practicum.bankui.web.dto.CashAction;
import ru.practicum.bankui.web.dto.CashResultDto;
import ru.practicum.bankui.web.dto.TransferResultDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private AccountClient accountClient;
    @Mock
    private CashClient cashClient;
    @Mock
    private TransferClient transferClient;
    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void depositAcceptsCommaAsDecimalSeparator() {
        when(cashClient.editCash(CashAction.PUT, new BigDecimal("10.50")))
                .thenReturn(new CashResultDto("ivanov", "DEPOSIT", new BigDecimal("10.50"), new BigDecimal("110.50")));

        assertThat(accountService.editCash(CashAction.PUT, "10,50")).isEqualTo("Положено 10.50 руб");
    }

    @Test
    void emptyAmountIsRejectedBeforeCallingServices() {
        assertThatThrownBy(() -> accountService.editCash(CashAction.GET, " "))
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Сумма должна быть заполнена");
        verifyNoInteractions(cashClient);
    }

    @Test
    void nonNumericAmountIsRejected() {
        assertThatThrownBy(() -> accountService.transfer("petrov", "сто"))
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Сумма должна быть числом");
        verifyNoInteractions(transferClient);
    }

    @Test
    void transferRequiresRecipient() {
        assertThatThrownBy(() -> accountService.transfer("", "100"))
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Выберите получателя перевода");
    }

    @Test
    void transferReportsRecipientName() {
        when(transferClient.transfer("petrov", new BigDecimal("100")))
                .thenReturn(new TransferResultDto("ivanov", "petrov", "Петров Петр", new BigDecimal("100"), BigDecimal.TEN));

        assertThat(accountService.transfer("petrov", "100")).isEqualTo("Успешно переведено 100 руб клиенту Петров Петр");
    }
}
