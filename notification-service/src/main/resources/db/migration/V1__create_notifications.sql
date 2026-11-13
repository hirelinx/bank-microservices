CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(64)              NOT NULL,
    message    VARCHAR(1024)            NOT NULL,
    sender     VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notifications_login ON notifications (login);
