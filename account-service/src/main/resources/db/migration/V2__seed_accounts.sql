-- Аккаунты для тестовых пользователей из keycloak/realms/bank-realm.json
INSERT INTO accounts (login, name, birthdate, balance)
VALUES ('ivanov', 'Иванов Иван', '2001-01-01', 1000.00),
       ('petrov', 'Петров Петр', '1995-05-15', 500.00),
       ('sidorov', 'Сидоров Сидор', '1988-10-20', 300.00);
