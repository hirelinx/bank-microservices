package ru.practicum.transfer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.transfer.client.dto.AccountTransferResultDto;
import ru.practicum.transfer.exception.RemoteOperationRejectedException;
import ru.practicum.transfer.exception.RemoteServiceException;

import java.math.BigDecimal;
import java.util.Map;

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

    /**
     * Снятие со счёта отправителя и зачисление получателю выполняются в Accounts одной транзакцией.
     */
    public AccountTransferResultDto transfer(String fromLogin, String toLogin, BigDecimal amount) {
        return circuitBreaker.run(() -> {
            try {
                return accountRestClient.post()
                        .uri("/account/internal/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("fromLogin", fromLogin, "toLogin", toLogin, "amount", amount))
                        .retrieve()
                        .body(AccountTransferResultDto.class);
            } catch (RestClientResponseException e) {
                if (RemoteOperationRejectedException.isRejection(e)) {
                    throw RemoteOperationRejectedException.from(e);
                }
                throw e;
            }
        }, throwable -> {
            if (throwable instanceof RemoteOperationRejectedException rejected) {
                throw rejected;
            }
            log.warn("Ошибка вызова account-service: {}", throwable.toString());
            throw RemoteServiceException.unavailable("аккаунтов");
        });
    }
}
