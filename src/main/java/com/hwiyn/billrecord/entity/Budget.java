package com.hwiyn.billrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budgets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Budget {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(nullable = false)
    private LocalDate month;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Budget create(
            UUID userId,
            UUID categoryId,
            LocalDate month,
            BigDecimal amount,
            String currency,
            Instant now) {
        Money money = Money.of(amount, currency);
        return new Budget(
                UUID.randomUUID(),
                userId,
                categoryId,
                month.withDayOfMonth(1),
                money.getAmount(),
                money.getCurrency(),
                now,
                now);
    }

    public void update(BigDecimal amount, String currency, Instant now) {
        if (amount != null) {
            this.amount = Money.of(amount, currency == null ? this.currency : currency).getAmount();
        }
        if (currency != null && !currency.isBlank()) {
            this.currency = Money.of(this.amount, currency).getCurrency();
        }
        this.updatedAt = now;
    }
}
