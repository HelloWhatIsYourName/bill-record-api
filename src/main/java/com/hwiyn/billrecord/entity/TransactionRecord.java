package com.hwiyn.billrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionRecord {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "transfer_account_id")
    private UUID transferAccountId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "transaction_time", nullable = false)
    private Instant transactionTime;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static TransactionRecord create(
            UUID userId,
            TransactionType type,
            UUID accountId,
            UUID transferAccountId,
            UUID categoryId,
            BigDecimal amount,
            String currency,
            Instant transactionTime,
            String note,
            Instant now) {
        Money money = Money.of(amount, currency);
        return new TransactionRecord(
                UUID.randomUUID(),
                userId,
                accountId,
                transferAccountId,
                categoryId,
                type,
                money.getAmount(),
                money.getCurrency(),
                transactionTime,
                blankToNull(note),
                now,
                now);
    }

    public void replaceWith(
            TransactionType type,
            UUID accountId,
            UUID transferAccountId,
            UUID categoryId,
            BigDecimal amount,
            String currency,
            Instant transactionTime,
            String note,
            Instant now) {
        Money money = Money.of(amount, currency);
        this.type = type;
        this.accountId = accountId;
        this.transferAccountId = transferAccountId;
        this.categoryId = categoryId;
        this.amount = money.getAmount();
        this.currency = money.getCurrency();
        this.transactionTime = transactionTime;
        this.note = blankToNull(note);
        this.updatedAt = now;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
