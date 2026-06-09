package com.hwiyn.billrecord.repository;

import com.hwiyn.billrecord.entity.Budget;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    boolean existsByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, LocalDate month);

    List<Budget> findByUserIdAndMonthOrderByCreatedAtAsc(UUID userId, LocalDate month);

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);
}
