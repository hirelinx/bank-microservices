package ru.practicum.cash.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Тело ProblemDetail, которое возвращают микросервисы банка.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiError(String detail, List<String> errors) {
}
