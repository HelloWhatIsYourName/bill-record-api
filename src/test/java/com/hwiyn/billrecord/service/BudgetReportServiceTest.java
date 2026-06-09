package com.hwiyn.billrecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hwiyn.billrecord.dto.account.AccountResponse;
import com.hwiyn.billrecord.dto.budget.BudgetResponse;
import com.hwiyn.billrecord.dto.budget.CreateBudgetRequest;
import com.hwiyn.billrecord.dto.report.CashFlowPointResponse;
import com.hwiyn.billrecord.dto.report.CategorySpendingResponse;
import com.hwiyn.billrecord.dto.report.MonthlySummaryResponse;
import com.hwiyn.billrecord.entity.AccountType;
import com.hwiyn.billrecord.entity.Budget;
import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.entity.TransactionRecord;
import com.hwiyn.billrecord.entity.TransactionType;
import com.hwiyn.billrecord.repository.BudgetRepository;
import com.hwiyn.billrecord.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetReportServiceTest {

    private static final UUID USER_ID = UUID.fromString("2e833469-a758-401a-9fc5-036ceaa67f50");
    private static final UUID ACCOUNT_ID = UUID.fromString("4c3cdd0d-ad71-4be7-a3d8-b151b6bf94bc");
    private static final UUID CATEGORY_ID = UUID.fromString("a238c84b-10ad-4f05-8bfb-2be7c0ed7095");
    private static final Instant NOW = Instant.parse("2026-06-09T00:00:00Z");
    private static final YearMonth MONTH = YearMonth.of(2026, 6);

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private AccountService accountService;

    private BudgetService budgetService;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        budgetService = new BudgetService(budgetRepository, transactionRepository, categoryService, clock);
        reportService = new ReportService(transactionRepository, accountService, categoryService);
    }

    @Test
    void listsMonthlyBudgetsWithSpentAndRemaining() {
        Category category = expenseCategory();
        Budget budget = Budget.create(USER_ID, CATEGORY_ID, LocalDate.parse("2026-06-01"), new BigDecimal("100.00"), "CNY", NOW);
        TransactionRecord expense = expense("30.00", Instant.parse("2026-06-10T08:00:00Z"));

        when(categoryService.requireCategory(USER_ID, CATEGORY_ID)).thenReturn(category);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(budgetRepository.findByUserIdAndMonthOrderByCreatedAtAsc(USER_ID, LocalDate.parse("2026-06-01")))
                .thenReturn(List.of(budget));
        when(transactionRepository.findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                USER_ID,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z")))
                .thenReturn(List.of(expense));

        budgetService.create(USER_ID, new CreateBudgetRequest(CATEGORY_ID, "2026-06", new BigDecimal("100.00"), "CNY"));
        List<BudgetResponse> responses = budgetService.list(USER_ID, MONTH);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().spent()).isEqualByComparingTo("30.00");
        assertThat(responses.getFirst().remaining()).isEqualByComparingTo("70.00");
    }

    @Test
    void monthlySummaryCalculatesTotalsAndBalances() {
        when(transactionRepository.findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                USER_ID,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z")))
                .thenReturn(List.of(
                        income("500.00", Instant.parse("2026-06-02T08:00:00Z")),
                        expense("80.00", Instant.parse("2026-06-03T08:00:00Z"))));
        when(accountService.listActive(USER_ID)).thenReturn(List.of(new AccountResponse(
                ACCOUNT_ID,
                "现金钱包",
                AccountType.CASH,
                BigDecimal.ZERO,
                new BigDecimal("420.00"),
                "CNY",
                false)));

        MonthlySummaryResponse response = reportService.monthlySummary(USER_ID, MONTH);

        assertThat(response.income()).isEqualByComparingTo("500.00");
        assertThat(response.expense()).isEqualByComparingTo("80.00");
        assertThat(response.net()).isEqualByComparingTo("420.00");
        assertThat(response.accountBalances()).hasSize(1);
    }

    @Test
    void categorySpendingAndCashFlowUseExpenseTransactions() {
        TransactionRecord expense = expense("30.00", Instant.parse("2026-06-10T08:00:00Z"));
        when(transactionRepository.findByUserIdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeAsc(
                USER_ID,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z")))
                .thenReturn(List.of(expense, income("10.00", Instant.parse("2026-06-10T10:00:00Z"))));
        when(categoryService.requireCategory(USER_ID, CATEGORY_ID)).thenReturn(expenseCategory());

        List<CategorySpendingResponse> spending = reportService.categorySpending(
                USER_ID,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z"));
        List<CashFlowPointResponse> cashFlow = reportService.cashFlow(
                USER_ID,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z"));

        assertThat(spending.getFirst().amount()).isEqualByComparingTo("30.00");
        assertThat(cashFlow.getFirst().date()).isEqualTo(LocalDate.parse("2026-06-10"));
        assertThat(cashFlow.getFirst().net()).isEqualByComparingTo("-20.00");
    }

    private static Category expenseCategory() {
        return Category.create(USER_ID, "餐饮", CategoryType.EXPENSE, null, null, NOW);
    }

    private static TransactionRecord expense(String amount, Instant transactionTime) {
        return TransactionRecord.create(
                USER_ID,
                TransactionType.EXPENSE,
                ACCOUNT_ID,
                null,
                CATEGORY_ID,
                new BigDecimal(amount),
                "CNY",
                transactionTime,
                null,
                NOW);
    }

    private static TransactionRecord income(String amount, Instant transactionTime) {
        return TransactionRecord.create(
                USER_ID,
                TransactionType.INCOME,
                ACCOUNT_ID,
                null,
                UUID.fromString("e02440cd-b71e-4ba5-9c6a-ea06325558fa"),
                new BigDecimal(amount),
                "CNY",
                transactionTime,
                null,
                NOW);
    }
}
