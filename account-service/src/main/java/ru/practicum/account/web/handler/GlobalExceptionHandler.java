package ru.practicum.account.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.account.exception.AccountNotFoundException;
import ru.practicum.account.exception.InsufficientFundsException;

import java.util.List;

/**
 * Все ошибки отдаются как ProblemDetail: detail — общий текст, errors — список сообщений для показа пользователю.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .toList();
        return problem(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, List.of("Некорректный формат запроса"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, List.of(e.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleNotFound(AccountNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, List.of(e.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException e) {
        return problem(HttpStatus.CONFLICT, List.of(e.getMessage()));
    }

    private ProblemDetail problem(HttpStatus status, List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, String.join("; ", errors));
        problem.setProperty("errors", errors);
        return problem;
    }
}
