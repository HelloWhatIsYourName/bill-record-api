package com.hwiyn.billrecord.repository;

import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.CategoryType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByUserIdAndTypeAndNameIgnoreCase(UUID userId, CategoryType type, String name);

    List<Category> findByUserIdAndArchivedFalseOrderByTypeAscNameAsc(UUID userId);

    List<Category> findByUserIdAndTypeAndArchivedFalseOrderByNameAsc(UUID userId, CategoryType type);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
}
