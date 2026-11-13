package ru.practicum.cash.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.cash.client.dto.ApiError;

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

    public static RemoteServiceException from(String serviceName, RestClientException e) {
        if (e instanceof RestClientResponseException response && isBusinessError(response.getStatusCode())) {
            return new RemoteServiceException(response.getStatusCode(), extractErrors(response));
        }
        String message = e instanceof ResourceAccessException
                ? "Сервис %s недоступен".formatted(serviceName)
                : "Ошибка при обращении к сервису %s".formatted(serviceName);
        return new RemoteServiceException(HttpStatus.BAD_GATEWAY, List.of(message));
    }

    // 401/403 означают проблему с сервисным токеном, а не ошибку пользователя
    private static boolean isBusinessError(HttpStatusCode status) {
        return status.is4xxClientError()
                && status.value() != HttpStatus.UNAUTHORIZED.value()
                && status.value() != HttpStatus.FORBIDDEN.value();
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
        return List.of(response.getStatusText());
    }
}
