package ru.practicum.bankui.web.client;

import ru.practicum.bankui.web.dto.AccountDto;
import ru.practicum.bankui.web.dto.UserDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Клиент сервиса Accounts. Текущий пользователь определяется сервисом по JWT, а не по параметру запроса.
 */
public interface AccountClient {
    UserDto getCurrentAccount();

    UserDto updateCurrentAccount(String name, LocalDate birthdate);

    List<AccountDto> getOtherAccounts();
}
