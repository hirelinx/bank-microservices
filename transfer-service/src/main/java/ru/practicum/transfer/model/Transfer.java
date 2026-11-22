package ru.practicum.transfer.model;

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
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_login", nullable = false)
    private String fromLogin;

    @Column(name = "to_login", nullable = false)
    private String toLogin;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;

    private String error;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Transfer(String fromLogin, String toLogin, BigDecimal amount, OperationStatus status, String error) {
        this.fromLogin = fromLogin;
        this.toLogin = toLogin;
        this.amount = amount;
        this.status = status;
        this.error = error;
        this.createdAt = Instant.now();
    }
}
