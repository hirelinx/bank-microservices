package ru.practicum.transfer.exception;

public class SelfTransferException extends RuntimeException {
    public SelfTransferException() {
        super("Нельзя перевести деньги на свой же счёт");
    }
}
