package ru.practicum.cash.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.cash.client.dto.ApiError;

import java.util.List;

/**
 * Другой микросервис отклонил операцию (4xx): например, недостаточно средств.
 * Не считается сбоем для Circuit Breaker.
 */
public class RemoteOperationRejectedException extends RemoteServiceException {

    public RemoteOperationRejectedException(HttpStatusCode status, List<String> errors) {
        super(status, errors);
    }

    /**
     * 401/403 означают проблему с сервисным токеном, а не ошибку пользователя, поэтому бизнес-отказом не являются.
     */
    public static boolean isRejection(RestClientResponseException e) {
        HttpStatusCode status = e.getStatusCode();
        return status.is4xxClientError()
                && status.value() != HttpStatus.UNAUTHORIZED.value()
                && status.value() != HttpStatus.FORBIDDEN.value();
    }

    public static RemoteOperationRejectedException from(RestClientResponseException e) {
        return new RemoteOperationRejectedException(e.getStatusCode(), extractErrors(e));
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
