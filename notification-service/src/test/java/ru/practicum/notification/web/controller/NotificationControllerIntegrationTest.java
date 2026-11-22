package ru.practicum.notification.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.practicum.notification.model.Notification;
import ru.practicum.notification.repository.NotificationRepository;
import ru.practicum.notification.support.AbstractIntegrationTest;
import ru.practicum.notification.support.TestTokens;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void storesNotificationWithSenderFromServiceToken() throws Exception {
        String login = "user-" + UUID.randomUUID();

        mockMvc.perform(post("/notifications")
                        .with(TestTokens.service("cash-service", "ROLE_SERVICE", "ACCESS_NOTIFICATIONS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "%s", "message": "Счёт пополнен на 100 руб."}
                                """.formatted(login)))
                .andExpect(status().isAccepted());

        assertThat(notificationRepository.findAll())
                .filteredOn(notification -> notification.getLogin().equals(login))
                .singleElement()
                .extracting(Notification::getSender, Notification::getMessage)
                .containsExactly("cash-service", "Счёт пополнен на 100 руб.");
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/notifications")
                        .with(TestTokens.service("cash-service", "ROLE_SERVICE", "ACCESS_NOTIFICATIONS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "ivanov", "message": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/notifications")
                        .with(TestTokens.user("ivanov"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "ivanov", "message": "spam"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void serviceWithoutNotificationsPrivilegeIsForbidden() throws Exception {
        mockMvc.perform(post("/notifications")
                        .with(TestTokens.service("some-service", "ROLE_SERVICE", "ACCESS_ACCOUNTS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "ivanov", "message": "text"}
                                """))
                .andExpect(status().isForbidden());
    }
}
