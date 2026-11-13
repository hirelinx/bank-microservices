package ru.practicum.bankui.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class BankServiceExceptionTest {

    @Test
    void takesErrorsFromProblemDetail() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/cash/withdraw"))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body("""
                        {"status": 409, "detail": "Недостаточно средств на счету", "errors": ["Недостаточно средств на счету"]}
                        """));

        HttpClientErrorException error = assertThrows(HttpClientErrorException.class,
                () -> builder.build().post().uri("/cash/withdraw").retrieve().toBodilessEntity());

        assertThat(BankServiceException.from(error).getErrors()).containsExactly("Недостаточно средств на счету");
    }

    @Test
    void reportsExpiredSession() {
        HttpClientErrorException unauthorized = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

        assertThat(BankServiceException.from(unauthorized).getErrors()).containsExactly("Сессия истекла, войдите заново");
    }

    @Test
    void reportsMissingPrivileges() {
        HttpClientErrorException forbidden = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN, "Forbidden", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

        assertThat(BankServiceException.from(forbidden).getErrors()).containsExactly("Недостаточно прав для выполнения операции");
    }

    @Test
    void reportsUnavailableService() {
        assertThat(BankServiceException.from(new ResourceAccessException("Connection refused")).getErrors())
                .containsExactly("Сервис временно недоступен, попробуйте позже");
    }
}
