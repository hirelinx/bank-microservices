CREATE TABLE transfers
(
    id         BIGSERIAL PRIMARY KEY,
    from_login VARCHAR(64)              NOT NULL,
    to_login   VARCHAR(64)              NOT NULL,
    amount     NUMERIC(19, 2)           NOT NULL,
    status     VARCHAR(16)              NOT NULL,
    error      VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_transfers_from_login ON transfers (from_login);
CREATE INDEX idx_transfers_to_login ON transfers (to_login);
