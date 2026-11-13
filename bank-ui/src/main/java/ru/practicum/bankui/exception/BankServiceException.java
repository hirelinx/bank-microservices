package ru.practicum.bankui.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.bankui.web.dto.ApiError;

import java.util.List;

/**
 * Ошибка выполнения операции, тексты которой показываются пользователю на странице.
 */
@Getter
public class BankServiceException extends RuntimeException {
    private final List<String> errors;

    public BankServiceException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = errors;
    }

    public BankServiceException(String error) {
        this(List.of(error));
    }

    public static BankServiceException from(RestClientException e) {
        if (e instanceof ResourceAccessException) {
            return new BankServiceException("Сервис временно недоступен, попробуйте позже");
        }
        if (!(e instanceof RestClientResponseException response)) {
            return new BankServiceException("Ошибка при выполнении операции");
        }
        if (response.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
            return new BankServiceException("Сессия истекла, войдите заново");
        }
        if (response.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
            return new BankServiceException("Недостаточно прав для выполнения операции");
        }
        return new BankServiceException(extractErrors(response));
    }

    private static List<String> extractErrors(RestClientResponseException response) {
        try {
            ApiError error = response.getResponseBodyAs(ApiError.class);
            if (error != null && error.errors() != null && !error.errors().isEmpty()) {
                return error.errors();
            }
            if (error != null && error.detail() != null) {
                return List.of(error.detail());
            }
        } catch (RuntimeException ignored) {
            // тело ответа не ProblemDetail
        }
        return List.of("Ошибка при выполнении операции: " + response.getStatusCode().value());
    }
}
