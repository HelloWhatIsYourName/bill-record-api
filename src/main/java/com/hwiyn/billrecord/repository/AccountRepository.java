package com.hwiyn.billrecord.repository;

import com.hwiyn.billrecord.entity.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    List<Account> findByUserIdAndArchivedFalseOrderByCreatedAtAsc(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}
