package ru.practicum.bankui.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.web.client.AccountClient;
import ru.practicum.bankui.web.client.CashClient;
import ru.practicum.bankui.web.client.TransferClient;
import ru.practicum.bankui.web.dto.AccountDto;
import ru.practicum.bankui.web.dto.CashAction;
import ru.practicum.bankui.web.dto.CashResultDto;
import ru.practicum.bankui.web.dto.TransferResultDto;
import ru.practicum.bankui.web.dto.UserDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
public class AccountServiceImpl {
    private final AccountClient accountClient;
    private final CashClient cashClient;
    private final TransferClient transferClient;

    public UserDto getCurrentAccount() {
        return accountClient.getCurrentAccount();
    }

    public List<AccountDto> getOtherAccounts() {
        return accountClient.getOtherAccounts();
    }

    public UserDto updateCurrentAccount(String name, LocalDate birthdate) {
        return accountClient.updateCurrentAccount(name, birthdate);
    }

    /**
     * @return текст об успешном выполнении операции
     */
    public String editCash(CashAction action, String value) {
        CashResultDto result = cashClient.editCash(action, parseAmount(value));
        return action == CashAction.GET
                ? "Снято %s руб".formatted(result.amount().toPlainString())
                : "Положено %s руб".formatted(result.amount().toPlainString());
    }

    /**
     * @return текст об успешном выполнении операции
     */
    public String transfer(String login, String value) {
        BigDecimal amount = parseAmount(value);
        if (login == null || login.isBlank()) {
            throw new BankServiceException("Выберите получателя перевода");
        }
        TransferResultDto result = transferClient.transfer(login, amount);
        return "Успешно переведено %s руб клиенту %s".formatted(result.amount().toPlainString(), result.toName());
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            throw new BankServiceException("Сумма должна быть заполнена");
        }
        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new BankServiceException("Сумма должна быть числом");
        }
    }
}
