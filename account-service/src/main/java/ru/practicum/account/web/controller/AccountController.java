package ru.practicum.account.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.account.client.NotificationClient;
import ru.practicum.account.service.AccountService;
import ru.practicum.account.web.dto.AccountDto;
import ru.practicum.account.web.dto.AccountShortDto;
import ru.practicum.account.web.dto.UpdateAccountRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Запросы пользователя (через Gateway с пользовательским JWT).
 * Логин всегда берётся из токена, поэтому пользователь видит сумму только на своём счёте.
 */
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final NotificationClient notificationClient;

    @GetMapping("/me")
    public AccountDto getCurrent(JwtAuthenticationToken authentication) {
        return accountService.getOrCreate(authentication.getName(), fullName(authentication.getToken()));
    }

    @PutMapping("/me")
    public AccountDto updateCurrent(JwtAuthenticationToken authentication,
                                    @Valid @RequestBody UpdateAccountRequest request) {
        String login = authentication.getName();
        accountService.getOrCreate(login, fullName(authentication.getToken()));
        AccountDto updated = accountService.update(login, request.name(), request.birthdate());
        notificationClient.notify(login, "Данные аккаунта обновлены");
        return updated;
    }

    @GetMapping("/others")
    public List<AccountShortDto> getOthers(JwtAuthenticationToken authentication) {
        return accountService.getOthers(authentication.getName());
    }

    private String fullName(Jwt jwt) {
        String fullName = Stream.of(jwt.getClaimAsString("family_name"), jwt.getClaimAsString("given_name"))
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));
        return fullName.isEmpty() ? null : fullName;
    }
}
