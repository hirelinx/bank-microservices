package ru.practicum.account.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.practicum.account.validation.Adult;

import java.time.LocalDate;

public record UpdateAccountRequest(
        @NotBlank(message = "Фамилия и имя должны быть заполнены")
        @Size(max = 255, message = "Фамилия и имя не должны превышать 255 символов")
        String name,

        @NotNull(message = "Дата рождения должна быть заполнена")
        @Adult
        LocalDate birthdate
) {
}
