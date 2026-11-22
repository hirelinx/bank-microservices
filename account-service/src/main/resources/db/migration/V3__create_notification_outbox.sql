-- Transactional Outbox: уведомление сохраняется в одной транзакции с изменением аккаунта
-- и отправляется в Notifications фоновым публикатором
CREATE TABLE notification_outbox
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(64)              NOT NULL,
    message    VARCHAR(1024)            NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notification_outbox_pending ON notification_outbox (id) WHERE sent_at IS NULL;
