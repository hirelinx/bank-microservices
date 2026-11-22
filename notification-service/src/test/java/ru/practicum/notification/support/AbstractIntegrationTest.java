package ru.practicum.notification.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Общая конфигурация интеграционных тестов: одинаковые аннотации у всех наследников
 * позволяют TestContext Framework переиспользовать закешированный контекст и контейнер PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestProfile
@Import(PostgresTestcontainersConfig.class)
public abstract class AbstractIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
}
