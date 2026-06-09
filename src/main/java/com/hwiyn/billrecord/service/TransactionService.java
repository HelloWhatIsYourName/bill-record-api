package com.hwiyn.billrecord.service;

import com.hwiyn.billrecord.dto.transaction.CreateTransactionRequest;
import com.hwiyn.billrecord.dto.transaction.TransactionResponse;
import com.hwiyn.billrecord.dto.transaction.UpdateTransactionRequest;
import com.hwiyn.billrecord.entity.Account;
import com.hwiyn.billrecord.entity.Category;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.entity.Money;
import com.hwiyn.billrecord.entity.TransactionRecord;
import com.hwiyn.billrecord.entity.TransactionType;
import com.hwiyn.billrecord.exception.ApiException;
import com.hwiyn.billrecord.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final Clock clock;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountService accountService,
            CategoryService categoryService,
            Clock clock) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.clock = clock;
    }

    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        ValidatedTransaction validated = validate(userId, request);
        Instant now = Instant.now(clock);

        TransactionRecord transaction = TransactionRecord.create(
                userId,
                request.type(),
                request.accountId(),
                request.transferAccountId(),
                request.categoryId(),
                request.amount(),
                currencyOrDefault(request.currency()),
                request.transactionTime(),
                request.note(),
                now);
        applyEffect(transaction, validated.source(), validated.transferTarget(), false, now);
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(
            UUID userId,
            TransactionType type,
            UUID accountId,
            UUID categoryId) {
        return transactionRepository.findByUserIdOrderByTransactionTimeDescCreatedAtDesc(userId).stream()
                .filter(transaction -> type == null || transaction.getType() == type)
                .filter(transaction -> accountId == null
                        || accountId.equals(transaction.getAccountId())
                        || accountId.equals(transaction.getTransferAccountId()))
                .filter(transaction -> categoryId == null || categoryId.equals(transaction.getCategoryId()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(UUID userId, UUID transactionId) {
        return toResponse(requireTransaction(userId, transactionId));
    }

    @Transactional
    public TransactionResponse update(UUID userId, UUID transactionId, UpdateTransactionRequest request) {
        TransactionRecord transaction = requireTransaction(userId, transactionId);
        Account oldSource = accountService.requireAccount(userId, transaction.getAccountId());
        Account oldTarget = transaction.getTransferAccountId() == null
                ? null
                : accountService.requireAccount(userId, transaction.getTransferAccountId());

        ValidatedTransaction validated = validate(userId, request);
        Instant now = Instant.now(clock);
        applyEffect(transaction, oldSource, oldTarget, true, now);
        transaction.replaceWith(
                request.type(),
                request.accountId(),
                request.transferAccountId(),
                request.categoryId(),
                request.amount(),
                currencyOrDefault(request.currency()),
                request.transactionTime(),
                request.note(),
                now);
        applyEffect(transaction, validated.source(), validated.transferTarget(), false, now);
        return toResponse(transaction);
    }

    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        TransactionRecord transaction = requireTransaction(userId, transactionId);
        Account source = accountService.requireAccount(userId, transaction.getAccountId());
        Account target = transaction.getTransferAccountId() == null
                ? null
                : accountService.requireAccount(userId, transaction.getTransferAccountId());
        applyEffect(transaction, source, target, true, Instant.now(clock));
        transactionRepository.delete(transaction);
    }

    public TransactionRecord requireTransaction(UUID userId, UUID transactionId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "TRANSACTION_NOT_FOUND",
                        "Transaction not found"));
    }

    public TransactionResponse toResponse(TransactionRecord transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAccountId(),
                transaction.getTransferAccountId(),
                transaction.getCategoryId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionTime(),
                transaction.getNote());
    }

    private ValidatedTransaction validate(UUID userId, CreateTransactionRequest request) {
        return validate(
                userId,
                request.type(),
                request.accountId(),
                request.transferAccountId(),
                request.categoryId(),
                request.currency());
    }

    private ValidatedTransaction validate(UUID userId, UpdateTransactionRequest request) {
        return validate(
                userId,
                request.type(),
                request.accountId(),
                request.transferAccountId(),
                request.categoryId(),
                request.currency());
    }

    private ValidatedTransaction validate(
            UUID userId,
            TransactionType type,
            UUID accountId,
            UUID transferAccountId,
            UUID categoryId,
            String requestedCurrency) {
        Account source = accountService.requireAccount(userId, accountId);
        String currency = currencyOrDefault(requestedCurrency);
        if (!source.getCurrency().equals(currency)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CURRENCY_MISMATCH", "Currency must match account currency");
        }

        Account target = null;
        if (type == TransactionType.TRANSFER) {
            if (transferAccountId == null || transferAccountId.equals(accountId)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_TRANSFER_ACCOUNT",
                        "Transfer target account is required");
            }
            target = accountService.requireAccount(userId, transferAccountId);
            if (!target.getCurrency().equals(currency)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "CURRENCY_MISMATCH",
                        "Transfer accounts must use the same currency");
            }
            if (categoryId != null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "TRANSFER_CATEGORY_NOT_ALLOWED",
                        "Transfer transaction must not have a category");
            }
            return new ValidatedTransaction(source, target);
        }

        if (categoryId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CATEGORY_REQUIRED", "Category is required");
        }
        Category category = categoryService.requireCategory(userId, categoryId);
        CategoryType expectedCategoryType = type == TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
        if (category.getType() != expectedCategoryType) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "CATEGORY_TYPE_MISMATCH",
                    "Category type does not match transaction type");
        }
        return new ValidatedTransaction(source, null);
    }

    private void applyEffect(TransactionRecord transaction, Account source, Account target, boolean reverse, Instant now) {
        BigDecimal amount = transaction.getAmount();
        if (reverse) {
            amount = amount.negate();
        }

        if (transaction.getType() == TransactionType.INCOME) {
            source.applyDelta(amount, now);
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            source.applyDelta(amount.negate(), now);
        } else {
            source.applyDelta(amount.negate(), now);
            if (target == null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_TRANSFER_ACCOUNT",
                        "Transfer target account is required");
            }
            target.applyDelta(amount, now);
        }
    }

    private String currencyOrDefault(String currency) {
        return currency == null || currency.isBlank() ? Money.DEFAULT_CURRENCY : currency.trim().toUpperCase();
    }

    private record ValidatedTransaction(Account source, Account transferTarget) {
    }
}
