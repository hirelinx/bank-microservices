package ru.practicum.account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String login) {
        super("Аккаунт '%s' не найден".formatted(login));
    }
}
