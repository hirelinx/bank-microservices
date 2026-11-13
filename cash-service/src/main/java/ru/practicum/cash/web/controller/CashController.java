package ru.practicum.cash.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.cash.service.CashService;
import ru.practicum.cash.web.dto.CashRequest;
import ru.practicum.cash.web.dto.CashResultDto;

/**
 * Операции выполняются только со счётом пользователя, логин которого указан в JWT.
 */
@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {
    private final CashService cashService;

    @PostMapping("/deposit")
    public CashResultDto deposit(JwtAuthenticationToken authentication, @Valid @RequestBody CashRequest request) {
        return cashService.deposit(authentication.getName(), request.amount());
    }

    @PostMapping("/withdraw")
    public CashResultDto withdraw(JwtAuthenticationToken authentication, @Valid @RequestBody CashRequest request) {
        return cashService.withdraw(authentication.getName(), request.amount());
    }
}
