package com.hwiyn.billrecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hwiyn.billrecord.dto.transaction.CreateTransactionRequest;
import com.hwiyn.billrecord.dto.transaction.TransactionResponse;
import com.hwiyn.billrecord.entity.Account;
import com.hwiyn.billrecord.entity.AccountType;
import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.entity.TransactionRecord;
import com.hwiyn.billrecord.entity.TransactionType;
import com.hwiyn.billrecord.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final UUID USER_ID = UUID.fromString("2e833469-a758-401a-9fc5-036ceaa67f50");
    private static final Instant NOW = Instant.parse("2026-06-09T00:00:00Z");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private CategoryService categoryService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                accountService,
                categoryService,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsExpenseAndSubtractsAccountBalance() {
        Account account = Account.create(USER_ID, "现金钱包", AccountType.CASH, new BigDecimal("100.00"), "CNY", NOW);
        Category category = Category.create(USER_ID, "餐饮", CategoryType.EXPENSE, null, null, NOW);
        CreateTransactionRequest request = new CreateTransactionRequest(
                TransactionType.EXPENSE,
                account.getId(),
                null,
                category.getId(),
                new BigDecimal("25.50"),
                "CNY",
                NOW,
                "Lunch");

        when(accountService.requireAccount(USER_ID, account.getId())).thenReturn(account);
        when(categoryService.requireCategory(USER_ID, category.getId())).thenReturn(category);
        when(transactionRepository.save(any(TransactionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.create(USER_ID, request);

        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.amount()).isEqualByComparingTo("25.50");
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("74.50");
    }

    @Test
    void createsTransferAndMovesBalanceBetweenAccounts() {
        Account source = Account.create(USER_ID, "现金钱包", AccountType.CASH, new BigDecimal("100.00"), "CNY", NOW);
        Account target = Account.create(USER_ID, "银行卡", AccountType.BANK, new BigDecimal("10.00"), "CNY", NOW);
        CreateTransactionRequest request = new CreateTransactionRequest(
                TransactionType.TRANSFER,
                source.getId(),
                target.getId(),
                null,
                new BigDecimal("40.00"),
                "CNY",
                NOW,
                "Move cash");

        when(accountService.requireAccount(USER_ID, source.getId())).thenReturn(source);
        when(accountService.requireAccount(USER_ID, target.getId())).thenReturn(target);
        when(transactionRepository.save(any(TransactionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.create(USER_ID, request);

        assertThat(source.getCurrentBalance()).isEqualByComparingTo("60.00");
        assertThat(target.getCurrentBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void deletesTransactionAndReversesBalanceEffect() {
        Account account = Account.create(USER_ID, "现金钱包", AccountType.CASH, new BigDecimal("100.00"), "CNY", NOW);
        Category category = Category.create(USER_ID, "餐饮", CategoryType.EXPENSE, null, null, NOW);
        TransactionRecord transaction = TransactionRecord.create(
                USER_ID,
                TransactionType.EXPENSE,
                account.getId(),
                null,
                category.getId(),
                new BigDecimal("30.00"),
                "CNY",
                NOW,
                null,
                NOW);
        account.applyDelta(new BigDecimal("-30.00"), NOW);

        when(transactionRepository.findByIdAndUserId(transaction.getId(), USER_ID)).thenReturn(Optional.of(transaction));
        when(accountService.requireAccount(USER_ID, account.getId())).thenReturn(account);

        transactionService.delete(USER_ID, transaction.getId());

        assertThat(account.getCurrentBalance()).isEqualByComparingTo("100.00");
    }
}
