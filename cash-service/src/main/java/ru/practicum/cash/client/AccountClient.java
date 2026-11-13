package ru.practicum.cash.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.cash.client.dto.BalanceDto;
import ru.practicum.cash.exception.RemoteOperationRejectedException;
import ru.practicum.cash.exception.RemoteServiceException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class AccountClient {
    private final RestClient accountRestClient;
    private final CircuitBreaker circuitBreaker;

    public AccountClient(@Qualifier("accountRestClient") RestClient accountRestClient,
                         CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.accountRestClient = accountRestClient;
        this.circuitBreaker = circuitBreakerFactory.create("account-service");
    }

    public BalanceDto deposit(String login, BigDecimal amount) {
        return changeBalance(login, "deposit", amount);
    }

    public BalanceDto withdraw(String login, BigDecimal amount) {
        return changeBalance(login, "withdraw", amount);
    }

    private BalanceDto changeBalance(String login, String operation, BigDecimal amount) {
        return circuitBreaker.run(() -> {
            try {
                return accountRestClient.post()
                        .uri("/account/internal/{login}/{operation}", login, operation)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("amount", amount))
                        .retrieve()
                        .body(BalanceDto.class);
            } catch (RestClientResponseException e) {
                if (RemoteOperationRejectedException.isRejection(e)) {
                    throw RemoteOperationRejectedException.from(e);
                }
                throw e;
            }
        }, throwable -> {
            Throwable cause = unwrap(throwable);
            if (cause instanceof RemoteOperationRejectedException rejected) {
                throw rejected;
            }
            log.warn("Ошибка вызова account-service: {}", cause.toString());
            throw RemoteServiceException.unavailable("аккаунтов");
        });
    }

    // Circuit Breaker выполняет вызов через executor и передаёт в fallback обёрнутое исключение
    private static Throwable unwrap(Throwable throwable) {
        boolean wrapped = throwable instanceof ExecutionException || throwable instanceof CompletionException;
        return wrapped && throwable.getCause() != null ? throwable.getCause() : throwable;
    }
}
