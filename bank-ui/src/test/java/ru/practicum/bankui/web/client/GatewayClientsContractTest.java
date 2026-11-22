package ru.practicum.bankui.web.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import ru.practicum.bankui.exception.BankServiceException;
import ru.practicum.bankui.web.dto.AccountDto;
import ru.practicum.bankui.web.dto.CashAction;
import ru.practicum.bankui.web.dto.UserDto;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Consumer-сторона контрактов фронта с account-service, cash-service и transfer-service.
 * Gateway пробрасывает пути без изменений, поэтому клиенты проверяются напрямую против stub-ов сервисов.
 */
class GatewayClientsContractTest {
    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .downloadStub("ru.practicum", "account-service", "+", "stubs")
            .downloadStub("ru.practicum", "cash-service", "+", "stubs")
            .downloadStub("ru.practicum", "transfer-service", "+", "stubs")
            .stubsMode(StubRunnerProperties.StubsMode.CLASSPATH);

    private final AccountClient accountClient = new AccountClientImpl(restClient("account-service"));
    private final CashClient cashClient = new CashClientImpl(restClient("cash-service"));
    private final TransferClient transferClient = new TransferClientImpl(restClient("transfer-service"));

    @Test
    void getsCurrentAccount() {
        assertThat(accountClient.getCurrentAccount())
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(new UserDto("ivanov", "Иванов Иван", LocalDate.of(2001, 1, 1), new BigDecimal("1000.00")));
    }

    @Test
    void updatesCurrentAccount() {
        assertThat(accountClient.updateCurrentAccount("Иванов Иван", LocalDate.of(2001, 1, 1)).name())
                .isEqualTo("Иванов Иван");
    }

    @Test
    void showsValidationErrorForUnderageUser() {
        assertThatThrownBy(() -> accountClient.updateCurrentAccount("Иванов Иван", LocalDate.of(2020, 1, 1)))
                .isInstanceOfSatisfying(BankServiceException.class, e ->
                        assertThat(e.getErrors()).containsExactly("Возраст должен быть старше 18 лет"));
    }

    @Test
    void getsOtherAccountsWithoutBalances() {
        assertThat(accountClient.getOtherAccounts()).containsExactly(
                new AccountDto("petrov", "Петров Петр"),
                new AccountDto("sidorov", "Сидоров Сидор"));
    }

    @Test
    void depositsCash() {
        assertThat(cashClient.editCash(CashAction.PUT, new BigDecimal("100")).balance()).isEqualByComparingTo("1100.00");
    }

    @Test
    void withdrawsCash() {
        assertThat(cashClient.editCash(CashAction.GET, new BigDecimal("100")).balance()).isEqualByComparingTo("900.00");
    }

    @Test
    void showsInsufficientFundsOnWithdrawal() {
        assertThatThrownBy(() -> cashClient.editCash(CashAction.GET, new BigDecimal("100000")))
                .isInstanceOfSatisfying(BankServiceException.class, e ->
                        assertThat(e.getErrors()).containsExactly("Недостаточно средств на счету"));
    }

    @Test
    void transfersMoney() {
        assertThat(transferClient.transfer("petrov", new BigDecimal("100")).toName()).isEqualTo("Петров Петр");
    }

    @Test
    void showsInsufficientFundsOnTransfer() {
        assertThatThrownBy(() -> transferClient.transfer("petrov", new BigDecimal("100000")))
                .isInstanceOfSatisfying(BankServiceException.class, e ->
                        assertThat(e.getErrors()).containsExactly("Недостаточно средств на счету"));
    }

    private static RestClient restClient(String service) {
        return RestClient.builder()
                .baseUrl(stubRunner.findStubUrl(service).toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer user-access-token")
                .build();
    }
}
