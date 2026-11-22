package ru.practicum.cash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.cash.model.CashOperation;

public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
}
