package ru.practicum.transfer.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import ru.practicum.transfer.model.OutboxNotification;

import java.util.List;

public interface OutboxNotificationRepository extends JpaRepository<OutboxNotification, Long> {

    /**
     * FOR UPDATE SKIP LOCKED: несколько экземпляров сервиса не отправят одно уведомление одновременно.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select n from OutboxNotification n where n.sentAt is null order by n.id")
    List<OutboxNotification> findPendingForUpdate(Pageable pageable);
}
