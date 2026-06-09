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
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountType type;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Account create(
            UUID userId,
            String name,
            AccountType type,
            BigDecimal openingBalance,
            String currency,
            Instant now) {
        Money money = Money.of(openingBalance, currency);
        return new Account(
                UUID.randomUUID(),
                userId,
                name.trim(),
                type,
                money.getCurrency(),
                money.getAmount(),
                money.getAmount(),
                false,
                now,
                now);
    }

    public void update(String name, AccountType type, Instant now) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (type != null) {
            this.type = type;
        }
        this.updatedAt = now;
    }

    public void applyDelta(BigDecimal delta, Instant now) {
        this.currentBalance = Money.of(currentBalance.add(delta), currency).getAmount();
        this.updatedAt = now;
    }

    public void archive(Instant now) {
        this.archived = true;
        this.updatedAt = now;
    }
}
