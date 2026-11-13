package ru.practicum.transfer.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.practicum.transfer.client.dto.AccountTransferResultDto;
import ru.practicum.transfer.exception.RemoteServiceException;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountClient {
    private final RestClient accountRestClient;

    public AccountClient(@Qualifier("accountRestClient") RestClient accountRestClient) {
        this.accountRestClient = accountRestClient;
    }

    /**
     * Снятие со счёта отправителя и зачисление получателю выполняются в Accounts одной транзакцией.
     */
    public AccountTransferResultDto transfer(String fromLogin, String toLogin, BigDecimal amount) {
        try {
            return accountRestClient.post()
                    .uri("/account/internal/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("fromLogin", fromLogin, "toLogin", toLogin, "amount", amount))
                    .retrieve()
                    .body(AccountTransferResultDto.class);
        } catch (RestClientException e) {
            throw RemoteServiceException.from("аккаунтов", e);
        }
    }
}
