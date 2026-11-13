package ru.practicum.cash.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_operations")
@Getter
@Setter
@NoArgsConstructor
public class CashOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String login;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashAction action;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;

    private String error;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CashOperation(String login, CashAction action, BigDecimal amount, OperationStatus status, String error) {
        this.login = login;
        this.action = action;
        this.amount = amount;
        this.status = status;
        this.error = error;
        this.createdAt = Instant.now();
    }
}
