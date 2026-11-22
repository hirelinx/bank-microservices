package ru.practicum.transfer.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.transfer.client.AccountClient;

/**
 * Общая конфигурация интеграционных тестов: одинаковые аннотации и mock-бины у всех наследников
 * позволяют TestContext Framework переиспользовать закешированный контекст и контейнер PostgreSQL.
 * Взаимодействие с Accounts проверяется отдельно контрактными тестами.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestProfile
@Import(PostgresTestcontainersConfig.class)
public abstract class AbstractIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected AccountClient accountClient;
}
