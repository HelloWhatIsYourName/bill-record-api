package com.hwiyn.billrecord.repository;

import com.hwiyn.billrecord.entity.TransactionRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionRecord, UUID> {

    Optional<TransactionRecord> findByIdAndUserId(UUID id, UUID userId);

    List<TransactionRecord> findByUserIdOrderByTransactionTimeDescCreatedAtDesc(UUID userId);

    List<TransactionRecord> findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
            UUID userId,
            Instant fromInclusive,
            Instant toExclusive);
}
