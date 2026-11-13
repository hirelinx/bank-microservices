package ru.practicum.cash.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.practicum.cash.client.dto.BalanceDto;
import ru.practicum.cash.exception.RemoteServiceException;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountClient {
    private final RestClient accountRestClient;

    public AccountClient(@Qualifier("accountRestClient") RestClient accountRestClient) {
        this.accountRestClient = accountRestClient;
    }

    public BalanceDto deposit(String login, BigDecimal amount) {
        return changeBalance(login, "deposit", amount);
    }

    public BalanceDto withdraw(String login, BigDecimal amount) {
        return changeBalance(login, "withdraw", amount);
    }

    private BalanceDto changeBalance(String login, String operation, BigDecimal amount) {
        try {
            return accountRestClient.post()
                    .uri("/account/internal/{login}/{operation}", login, operation)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .body(BalanceDto.class);
        } catch (RestClientException e) {
            throw RemoteServiceException.from("аккаунтов", e);
        }
    }
}
