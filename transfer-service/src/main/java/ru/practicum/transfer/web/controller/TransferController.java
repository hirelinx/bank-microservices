package ru.practicum.transfer.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.transfer.service.TransferService;
import ru.practicum.transfer.web.dto.TransferRequest;
import ru.practicum.transfer.web.dto.TransferResultDto;

/**
 * Отправитель — всегда пользователь, логин которого указан в JWT.
 */
@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public TransferResultDto transfer(JwtAuthenticationToken authentication, @Valid @RequestBody TransferRequest request) {
        return transferService.transfer(authentication.getName(), request.toLogin(), request.amount());
    }
}
