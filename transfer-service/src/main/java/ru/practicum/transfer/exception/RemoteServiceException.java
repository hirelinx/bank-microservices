package ru.practicum.transfer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.List;

/**
 * Ошибка вызова другого микросервиса, которую нужно вернуть пользователю.
 */
@Getter
public class RemoteServiceException extends RuntimeException {
    private final HttpStatusCode status;
    private final List<String> errors;

    public RemoteServiceException(HttpStatusCode status, List<String> errors) {
        super(String.join("; ", errors));
        this.status = status;
        this.errors = errors;
    }

    public static RemoteServiceException unavailable(String serviceName) {
        return new RemoteServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                List.of("Сервис %s временно недоступен, попробуйте позже".formatted(serviceName)));
    }
}
