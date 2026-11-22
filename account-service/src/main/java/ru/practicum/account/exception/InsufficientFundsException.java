package ru.practicum.account.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Недостаточно средств на счету");
    }
}
