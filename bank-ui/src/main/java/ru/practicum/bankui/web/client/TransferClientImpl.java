package ru.practicum.bankui.web.client;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.web.dto.TransferResultDto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class TransferClientImpl implements TransferClient {
    private final RestClient gatewayRestClient;

    @Override
    public TransferResultDto transfer(String toLogin, BigDecimal amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("toLogin", toLogin);
        body.put("amount", amount);
        try {
            return gatewayRestClient.post()
                    .uri("/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TransferResultDto.class);
        } catch (RestClientException e) {
            throw BankServiceException.from(e);
        }
    }
}
