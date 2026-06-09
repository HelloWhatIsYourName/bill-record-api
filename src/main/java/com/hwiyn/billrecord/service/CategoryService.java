package com.hwiyn.billrecord.service;

import com.hwiyn.billrecord.dto.category.CategoryResponse;
import com.hwiyn.billrecord.dto.category.CreateCategoryRequest;
import com.hwiyn.billrecord.dto.category.UpdateCategoryRequest;
import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.exception.ApiException;
import com.hwiyn.billrecord.repository.CategoryRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("餐饮", CategoryType.EXPENSE, "#EF4444", "utensils"),
            new DefaultCategory("交通", CategoryType.EXPENSE, "#F97316", "bus"),
            new DefaultCategory("购物", CategoryType.EXPENSE, "#8B5CF6", "shopping-bag"),
            new DefaultCategory("居家", CategoryType.EXPENSE, "#10B981", "home"),
            new DefaultCategory("工资", CategoryType.INCOME, "#2563EB", "wallet"),
            new DefaultCategory("奖金", CategoryType.INCOME, "#14B8A6", "badge-plus"));

    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public CategoryService(CategoryRepository categoryRepository, Clock clock) {
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByUserIdAndTypeAndNameIgnoreCase(userId, request.type(), name)) {
            throw new ApiException(HttpStatus.CONFLICT, "CATEGORY_NAME_EXISTS", "Category name already exists");
        }
        Category category = Category.create(
                userId,
                name,
                request.type(),
                request.color(),
                request.icon(),
                Instant.now(clock));
        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(UUID userId, CategoryType type) {
        List<Category> categories = type == null
                ? categoryRepository.findByUserIdAndArchivedFalseOrderByTypeAscNameAsc(userId)
                : categoryRepository.findByUserIdAndTypeAndArchivedFalseOrderByNameAsc(userId, type);
        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID userId, UUID categoryId) {
        return toResponse(requireCategory(userId, categoryId));
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        Category category = requireCategory(userId, categoryId);
        String requestedName = request.name();
        if (requestedName != null
                && !requestedName.isBlank()
                && !category.getName().equalsIgnoreCase(requestedName.trim())
                && categoryRepository.existsByUserIdAndTypeAndNameIgnoreCase(
                        userId, category.getType(), requestedName.trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "CATEGORY_NAME_EXISTS", "Category name already exists");
        }
        category.update(requestedName, request.color(), request.icon(), Instant.now(clock));
        return toResponse(category);
    }

    @Transactional
    public void archive(UUID userId, UUID categoryId) {
        requireCategory(userId, categoryId).archive(Instant.now(clock));
    }

    @Transactional
    public void seedDefaultsForUser(UUID userId, Instant now) {
        List<Category> defaults = DEFAULT_CATEGORIES.stream()
                .filter(defaultCategory -> !categoryRepository.existsByUserIdAndTypeAndNameIgnoreCase(
                        userId, defaultCategory.type(), defaultCategory.name()))
                .map(defaultCategory -> Category.create(
                        userId,
                        defaultCategory.name(),
                        defaultCategory.type(),
                        defaultCategory.color(),
                        defaultCategory.icon(),
                        now))
                .toList();
        if (!defaults.isEmpty()) {
            categoryRepository.saveAll(defaults);
        }
    }

    public Category requireCategory(UUID userId, UUID categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .filter(category -> !category.isArchived())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Category not found"));
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColor(),
                category.getIcon(),
                category.isArchived());
    }

    private record DefaultCategory(String name, CategoryType type, String color, String icon) {
    }
}
