package ru.practicum.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.account.exception.AccountNotFoundException;
import ru.practicum.account.exception.InsufficientFundsException;
import ru.practicum.account.model.Account;
import ru.practicum.account.model.OutboxNotification;
import ru.practicum.account.repository.AccountRepository;
import ru.practicum.account.repository.OutboxNotificationRepository;
import ru.practicum.account.web.dto.AccountDto;
import ru.practicum.account.web.dto.TransferResultDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OutboxNotificationRepository outboxRepository;
    @InjectMocks
    private AccountService accountService;

    @Test
    void createsAccountOnFirstAccess() {
        when(accountRepository.findByLogin("ivanov")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountDto account = accountService.getOrCreate("ivanov", "Иванов Иван");

        assertThat(account).isEqualTo(new AccountDto("ivanov", "Иванов Иван", null, BigDecimal.ZERO));
    }

    @Test
    void updateSavesNotificationToOutbox() {
        Account account = account("ivanov", "100.00");
        when(accountRepository.findByLoginForUpdate("ivanov")).thenReturn(Optional.of(account));

        AccountDto updated = accountService.update("ivanov", null, "  Иванов Пётр ", LocalDate.of(1990, 1, 1));

        assertThat(updated.name()).isEqualTo("Иванов Пётр");
        assertThat(updated.birthdate()).isEqualTo(LocalDate.of(1990, 1, 1));
        ArgumentCaptor<OutboxNotification> notification = ArgumentCaptor.forClass(OutboxNotification.class);
        verify(outboxRepository).save(notification.capture());
        assertThat(notification.getValue().getLogin()).isEqualTo("ivanov");
        assertThat(notification.getValue().getSentAt()).isNull();
    }

    @Test
    void withdrawRejectsAmountGreaterThanBalance() {
        Account account = account("ivanov", "100.00");
        when(accountRepository.findByLoginForUpdate("ivanov")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw("ivanov", new BigDecimal("100.01")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void depositToUnknownAccountFails() {
        when(accountRepository.findByLoginForUpdate("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deposit("ghost", BigDecimal.ONE))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transferMovesMoneyAndLocksAccountsInLoginOrder() {
        Account petrov = account("petrov", "50.00");
        Account ivanov = account("ivanov", "10.00");
        ivanov.setName("Иванов Иван");
        when(accountRepository.findByLoginForUpdate("ivanov")).thenReturn(Optional.of(ivanov));
        when(accountRepository.findByLoginForUpdate("petrov")).thenReturn(Optional.of(petrov));

        TransferResultDto result = accountService.transfer("petrov", "ivanov", new BigDecimal("20.00"));

        assertThat(result).isEqualTo(new TransferResultDto("petrov", new BigDecimal("30.00"), "ivanov", "Иванов Иван"));
        assertThat(ivanov.getBalance()).isEqualByComparingTo("30.00");
        InOrder lockOrder = inOrder(accountRepository);
        lockOrder.verify(accountRepository).findByLoginForUpdate("ivanov");
        lockOrder.verify(accountRepository).findByLoginForUpdate("petrov");
    }

    @Test
    void transferToSelfIsRejected() {
        assertThatThrownBy(() -> accountService.transfer("ivanov", "ivanov", BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        verify(accountRepository, never()).findByLoginForUpdate(any());
    }

    @Test
    void othersUseLoginWhenNameIsEmpty() {
        when(accountRepository.findAllByLoginNotOrderByNameAsc("ivanov"))
                .thenReturn(List.of(account("petrov", "0")));

        assertThat(accountService.getOthers("ivanov"))
                .singleElement()
                .satisfies(other -> assertThat(other.name()).isEqualTo("petrov"));
    }

    private Account account(String login, String balance) {
        Account account = new Account(login, null);
        account.setBalance(new BigDecimal(balance));
        return account;
    }
}
