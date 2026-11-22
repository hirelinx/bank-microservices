package ru.practicum.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.account.exception.AccountNotFoundException;
import ru.practicum.account.exception.InsufficientFundsException;
import ru.practicum.account.model.Account;
import ru.practicum.account.model.OutboxNotification;
import ru.practicum.account.repository.AccountRepository;
import ru.practicum.account.repository.OutboxNotificationRepository;
import ru.practicum.account.web.dto.AccountDto;
import ru.practicum.account.web.dto.AccountShortDto;
import ru.practicum.account.web.dto.BalanceDto;
import ru.practicum.account.web.dto.TransferResultDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final OutboxNotificationRepository outboxRepository;

    /**
     * Аккаунт связывается с пользователем Keycloak по логину и создаётся при первом обращении.
     */
    @Transactional
    public AccountDto getOrCreate(String login, String defaultName) {
        Account account = accountRepository.findByLogin(login)
                .orElseGet(() -> accountRepository.save(new Account(login, defaultName)));
        return toDto(account);
    }

    @Transactional
    public AccountDto update(String login, String defaultName, String name, LocalDate birthdate) {
        Account account = accountRepository.findByLoginForUpdate(login)
                .orElseGet(() -> accountRepository.save(new Account(login, defaultName)));
        account.setName(name.trim());
        account.setBirthdate(birthdate);
        outboxRepository.save(new OutboxNotification(login, "Данные аккаунта обновлены"));
        return toDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountShortDto> getOthers(String login) {
        return accountRepository.findAllByLoginNotOrderByNameAsc(login).stream()
                .map(account -> new AccountShortDto(account.getLogin(), displayName(account)))
                .toList();
    }

    @Transactional
    public BalanceDto deposit(String login, BigDecimal amount) {
        Account account = lock(login);
        account.setBalance(account.getBalance().add(amount));
        return new BalanceDto(login, account.getBalance());
    }

    @Transactional
    public BalanceDto withdraw(String login, BigDecimal amount) {
        Account account = lock(login);
        debit(account, amount);
        return new BalanceDto(login, account.getBalance());
    }

    @Transactional
    public TransferResultDto transfer(String fromLogin, String toLogin, BigDecimal amount) {
        if (fromLogin.equals(toLogin)) {
            throw new IllegalArgumentException("Нельзя перевести деньги на свой же счёт");
        }

        // Блокируем строки в одном и том же порядке, чтобы встречные переводы не приводили к дедлоку
        boolean fromFirst = fromLogin.compareTo(toLogin) < 0;
        Account first = lock(fromFirst ? fromLogin : toLogin);
        Account second = lock(fromFirst ? toLogin : fromLogin);
        Account from = fromFirst ? first : second;
        Account to = fromFirst ? second : first;

        debit(from, amount);
        to.setBalance(to.getBalance().add(amount));

        return new TransferResultDto(from.getLogin(), from.getBalance(), to.getLogin(), displayName(to));
    }

    private Account lock(String login) {
        return accountRepository.findByLoginForUpdate(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
    }

    private void debit(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        account.setBalance(account.getBalance().subtract(amount));
    }

    private AccountDto toDto(Account account) {
        return new AccountDto(account.getLogin(), account.getName(), account.getBirthdate(), account.getBalance());
    }

    private String displayName(Account account) {
        return account.getName() == null || account.getName().isBlank() ? account.getLogin() : account.getName();
    }
}
