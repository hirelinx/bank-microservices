package ru.practicum.transfer.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.transfer.exception.RemoteServiceException;
import ru.practicum.transfer.exception.SelfTransferException;

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

    @ExceptionHandler(SelfTransferException.class)
    public ProblemDetail handleSelfTransfer(SelfTransferException e) {
        return problem(HttpStatus.BAD_REQUEST, List.of(e.getMessage()));
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ProblemDetail handleRemote(RemoteServiceException e) {
        return problem(e.getStatus(), e.getErrors());
    }

    private ProblemDetail problem(HttpStatusCode status, List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, String.join("; ", errors));
        problem.setProperty("errors", errors);
        return problem;
    }
}
