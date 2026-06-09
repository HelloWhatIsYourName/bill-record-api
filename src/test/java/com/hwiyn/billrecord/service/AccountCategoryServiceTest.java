package com.hwiyn.billrecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hwiyn.billrecord.dto.account.AccountResponse;
import com.hwiyn.billrecord.dto.account.CreateAccountRequest;
import com.hwiyn.billrecord.dto.category.CategoryResponse;
import com.hwiyn.billrecord.dto.category.CreateCategoryRequest;
import com.hwiyn.billrecord.entity.Account;
import com.hwiyn.billrecord.entity.AccountType;
import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.exception.ApiException;
import com.hwiyn.billrecord.repository.AccountRepository;
import com.hwiyn.billrecord.repository.CategoryRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AccountCategoryServiceTest {

    private static final UUID USER_ID = UUID.fromString("2e833469-a758-401a-9fc5-036ceaa67f50");
    private static final Instant NOW = Instant.parse("2026-06-09T00:00:00Z");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private AccountService accountService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        accountService = new AccountService(accountRepository, clock);
        categoryService = new CategoryService(categoryRepository, clock);
    }

    @Test
    void createsAccountWithOpeningBalanceAsCurrentBalance() {
        CreateAccountRequest request = new CreateAccountRequest(
                "现金钱包",
                AccountType.CASH,
                new BigDecimal("100.50"),
                null);

        when(accountRepository.existsByUserIdAndNameIgnoreCase(USER_ID, "现金钱包")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.create(USER_ID, request);

        assertThat(response.name()).isEqualTo("现金钱包");
        assertThat(response.type()).isEqualTo(AccountType.CASH);
        assertThat(response.openingBalance()).isEqualByComparingTo("100.50");
        assertThat(response.currentBalance()).isEqualByComparingTo("100.50");
        assertThat(response.currency()).isEqualTo("CNY");

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().isArchived()).isFalse();
    }

    @Test
    void rejectsDuplicateAccountNameForUser() {
        CreateAccountRequest request = new CreateAccountRequest(
                "现金钱包",
                AccountType.CASH,
                BigDecimal.ZERO,
                "CNY");
        when(accountRepository.existsByUserIdAndNameIgnoreCase(USER_ID, "现金钱包")).thenReturn(true);

        assertThatThrownBy(() -> accountService.create(USER_ID, request))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getCode()).isEqualTo("ACCOUNT_NAME_EXISTS");
                });
    }

    @Test
    void listsOnlyActiveAccounts() {
        Account cash = Account.create(USER_ID, "现金钱包", AccountType.CASH, new BigDecimal("20.00"), "CNY", NOW);
        when(accountRepository.findByUserIdAndArchivedFalseOrderByCreatedAtAsc(USER_ID)).thenReturn(List.of(cash));

        List<AccountResponse> responses = accountService.listActive(USER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().name()).isEqualTo("现金钱包");
    }

    @Test
    void createsCategoryAndSeedsDefaultCategories() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "餐饮",
                CategoryType.EXPENSE,
                "#EF4444",
                "utensils");
        when(categoryRepository.existsByUserIdAndTypeAndNameIgnoreCase(USER_ID, CategoryType.EXPENSE, "餐饮"))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.create(USER_ID, request);

        assertThat(response.name()).isEqualTo("餐饮");
        assertThat(response.type()).isEqualTo(CategoryType.EXPENSE);
        assertThat(response.archived()).isFalse();

        categoryService.seedDefaultsForUser(USER_ID, NOW);

        verify(categoryRepository).saveAll(any());
    }
}
