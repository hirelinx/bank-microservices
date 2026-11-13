package ru.practicum.notification.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.notification.service.NotificationService;
import ru.practicum.notification.web.dto.NotificationRequest;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(JwtAuthenticationToken authentication, @Valid @RequestBody NotificationRequest request) {
        // azp — client_id микросервиса, получившего токен по Client Credentials
        notificationService.send(request.login(), request.message(), authentication.getToken().getClaimAsString("azp"));
    }
}
