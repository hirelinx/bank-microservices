CREATE TABLE cash_operations
(
    id         BIGSERIAL PRIMARY KEY,
    login      VARCHAR(64)              NOT NULL,
    action     VARCHAR(16)              NOT NULL,
    amount     NUMERIC(19, 2)           NOT NULL,
    status     VARCHAR(16)              NOT NULL,
    error      VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_cash_operations_login ON cash_operations (login);
