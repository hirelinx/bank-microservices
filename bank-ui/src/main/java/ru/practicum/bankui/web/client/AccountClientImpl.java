package ru.practicum.bankui.web.client;

import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.web.dto.AccountDto;
import ru.practicum.bankui.web.dto.UserDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class AccountClientImpl implements AccountClient {
    private final RestClient gatewayRestClient;

    @Override
    public UserDto getCurrentAccount() {
        try {
            return gatewayRestClient.get()
                    .uri(account("/me"))
                    .retrieve()
                    .body(UserDto.class);
        } catch (RestClientException e) {
            throw BankServiceException.from(e);
        }
    }

    @Override
    public UserDto updateCurrentAccount(String name, LocalDate birthdate) {
        // HashMap, потому что Map.of не допускает null, а пустые поля должен отвергнуть сервис
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("birthdate", birthdate);
        try {
            return gatewayRestClient.put()
                    .uri(account("/me"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(UserDto.class);
        } catch (RestClientException e) {
            throw BankServiceException.from(e);
        }
    }

    @Override
    public List<AccountDto> getOtherAccounts() {
        try {
            List<AccountDto> accounts = gatewayRestClient.get()
                    .uri(account("/others"))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return accounts == null ? List.of() : accounts;
        } catch (RestClientException e) {
            throw BankServiceException.from(e);
        }
    }

    private String account(String uri) {return "/account" + uri;}

}
