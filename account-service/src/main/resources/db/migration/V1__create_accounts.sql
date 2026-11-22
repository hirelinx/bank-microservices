CREATE TABLE accounts
(
    id        BIGSERIAL PRIMARY KEY,
    login     VARCHAR(64)    NOT NULL UNIQUE,
    name      VARCHAR(255),
    birthdate DATE,
    balance   NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (balance >= 0)
);
