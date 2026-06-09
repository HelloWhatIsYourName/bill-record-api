package com.hwiyn.billrecord.service;

import com.hwiyn.billrecord.dto.budget.BudgetResponse;
import com.hwiyn.billrecord.dto.budget.CreateBudgetRequest;
import com.hwiyn.billrecord.dto.budget.UpdateBudgetRequest;
import com.hwiyn.billrecord.entity.Budget;
import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.entity.Money;
import com.hwiyn.billrecord.entity.TransactionRecord;
import com.hwiyn.billrecord.entity.TransactionType;
import com.hwiyn.billrecord.exception.ApiException;
import com.hwiyn.billrecord.repository.BudgetRepository;
import com.hwiyn.billrecord.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final Clock clock;

    public BudgetService(
            BudgetRepository budgetRepository,
            TransactionRepository transactionRepository,
            CategoryService categoryService,
            Clock clock) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.clock = clock;
    }

    @Transactional
    public BudgetResponse create(UUID userId, CreateBudgetRequest request) {
        YearMonth month = parseMonth(request.month());
        LocalDate firstDay = month.atDay(1);
        Category category = categoryService.requireCategory(userId, request.categoryId());
        if (category.getType() != CategoryType.EXPENSE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUDGET_CATEGORY_INVALID", "Budget category must be EXPENSE");
        }
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonth(userId, request.categoryId(), firstDay)) {
            throw new ApiException(HttpStatus.CONFLICT, "BUDGET_ALREADY_EXISTS", "Budget already exists");
        }
        Budget budget = Budget.create(
                userId,
                request.categoryId(),
                firstDay,
                request.amount(),
                request.currency() == null ? Money.DEFAULT_CURRENCY : request.currency(),
                Instant.now(clock));
        return toResponse(budgetRepository.save(budget), BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(UUID userId, YearMonth month) {
        LocalDate firstDay = month.atDay(1);
        List<TransactionRecord> transactions = monthTransactions(userId, month);
        return budgetRepository.findByUserIdAndMonthOrderByCreatedAtAsc(userId, firstDay).stream()
                .map(budget -> toResponse(budget, spentForBudget(budget, transactions)))
                .toList();
    }

    @Transactional
    public BudgetResponse update(UUID userId, UUID budgetId, UpdateBudgetRequest request) {
        Budget budget = requireBudget(userId, budgetId);
        budget.update(request.amount(), request.currency(), Instant.now(clock));
        return toResponse(budget, spentForBudget(budget, monthTransactions(userId, YearMonth.from(budget.getMonth()))));
    }

    @Transactional
    public void delete(UUID userId, UUID budgetId) {
        budgetRepository.delete(requireBudget(userId, budgetId));
    }

    public static YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MONTH", "Month must use yyyy-MM");
        }
    }

    private Budget requireBudget(UUID userId, UUID budgetId) {
        return budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BUDGET_NOT_FOUND", "Budget not found"));
    }

    private List<TransactionRecord> monthTransactions(UUID userId, YearMonth month) {
        Instant from = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return transactionRepository
                .findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                        userId, from, to);
    }

    private BigDecimal spentForBudget(Budget budget, List<TransactionRecord> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .filter(transaction -> budget.getCategoryId().equals(transaction.getCategoryId()))
                .map(TransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BudgetResponse toResponse(Budget budget, BigDecimal spent) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategoryId(),
                YearMonth.from(budget.getMonth()).toString(),
                budget.getAmount(),
                budget.getCurrency(),
                spent,
                budget.getAmount().subtract(spent));
    }
}
