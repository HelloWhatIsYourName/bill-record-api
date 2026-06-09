package com.hwiyn.billrecord.service;

import com.hwiyn.billrecord.dto.report.AccountBalanceResponse;
import com.hwiyn.billrecord.dto.report.CashFlowPointResponse;
import com.hwiyn.billrecord.dto.report.CategorySpendingResponse;
import com.hwiyn.billrecord.dto.report.MonthlySummaryResponse;
import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.TransactionRecord;
import com.hwiyn.billrecord.entity.TransactionType;
import com.hwiyn.billrecord.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;

    public ReportService(
            TransactionRepository transactionRepository,
            AccountService accountService,
            CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(UUID userId, YearMonth month) {
        List<TransactionRecord> transactions = monthTransactions(userId, month);
        BigDecimal income = total(transactions, TransactionType.INCOME);
        BigDecimal expense = total(transactions, TransactionType.EXPENSE);
        List<AccountBalanceResponse> balances = accountService.listActive(userId).stream()
                .map(account -> new AccountBalanceResponse(
                        account.id(),
                        account.name(),
                        account.currentBalance(),
                        account.currency()))
                .toList();
        return new MonthlySummaryResponse(month.toString(), income, expense, income.subtract(expense), balances);
    }

    @Transactional(readOnly = true)
    public List<CategorySpendingResponse> categorySpending(UUID userId, Instant from, Instant to) {
        Map<UUID, BigDecimal> amounts = new LinkedHashMap<>();
        transactionRepository.findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                        userId, from, to).stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .forEach(transaction -> amounts.merge(transaction.getCategoryId(), transaction.getAmount(), BigDecimal::add));

        return amounts.entrySet().stream()
                .map(entry -> {
                    Category category = categoryService.requireCategory(userId, entry.getKey());
                    return new CategorySpendingResponse(
                            category.getId(),
                            category.getName(),
                            entry.getValue(),
                            "CNY");
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CashFlowPointResponse> cashFlow(UUID userId, Instant from, Instant to) {
        Map<LocalDate, CashFlowAccumulator> daily = new LinkedHashMap<>();
        transactionRepository.findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                        userId, from, to).stream()
                .filter(transaction -> transaction.getType() != TransactionType.TRANSFER)
                .forEach(transaction -> {
                    LocalDate date = LocalDate.ofInstant(transaction.getTransactionTime(), ZoneOffset.UTC);
                    CashFlowAccumulator accumulator = daily.computeIfAbsent(date, ignored -> new CashFlowAccumulator());
                    if (transaction.getType() == TransactionType.INCOME) {
                        accumulator.income = accumulator.income.add(transaction.getAmount());
                    } else {
                        accumulator.expense = accumulator.expense.add(transaction.getAmount());
                    }
                });

        List<CashFlowPointResponse> responses = new ArrayList<>();
        daily.forEach((date, accumulator) -> responses.add(new CashFlowPointResponse(
                date,
                accumulator.income,
                accumulator.expense,
                accumulator.income.subtract(accumulator.expense))));
        return responses;
    }

    private List<TransactionRecord> monthTransactions(UUID userId, YearMonth month) {
        Instant from = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return transactionRepository
                .findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                        userId, from, to);
    }

    private BigDecimal total(List<TransactionRecord> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(TransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static final class CashFlowAccumulator {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;
    }
}
