package ru.practicum.bankui.web.client;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.web.dto.CashAction;
import ru.practicum.bankui.web.dto.CashResultDto;

import java.math.BigDecimal;
import java.util.Map;

@Component
@AllArgsConstructor
public class CashClientImpl implements CashClient {
    private final RestClient gatewayRestClient;

    @Override
    public CashResultDto editCash(CashAction action, BigDecimal amount) {
        String operation = action == CashAction.PUT ? "deposit" : "withdraw";
        try {
            return gatewayRestClient.post()
                    .uri("/cash/{operation}", operation)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .body(CashResultDto.class);
        } catch (RestClientException e) {
            throw BankServiceException.from(e);
        }
    }
}
