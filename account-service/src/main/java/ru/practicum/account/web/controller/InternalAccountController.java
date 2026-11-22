package ru.practicum.account.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.account.service.AccountService;
import ru.practicum.account.web.dto.AmountRequest;
import ru.practicum.account.web.dto.BalanceDto;
import ru.practicum.account.web.dto.TransferRequest;
import ru.practicum.account.web.dto.TransferResultDto;

/**
 * Изменение баланса. Доступно только микросервисам (Cash, Transfer) с сервисным JWT (Client Credentials).
 */
@RestController
@RequestMapping("/account/internal")
@RequiredArgsConstructor
public class InternalAccountController {
    private final AccountService accountService;

    @PostMapping("/{login}/deposit")
    public BalanceDto deposit(@PathVariable String login, @Valid @RequestBody AmountRequest request) {
        return accountService.deposit(login, request.amount());
    }

    @PostMapping("/{login}/withdraw")
    public BalanceDto withdraw(@PathVariable String login, @Valid @RequestBody AmountRequest request) {
        return accountService.withdraw(login, request.amount());
    }

    @PostMapping("/transfer")
    public TransferResultDto transfer(@Valid @RequestBody TransferRequest request) {
        return accountService.transfer(request.fromLogin(), request.toLogin(), request.amount());
    }
}
