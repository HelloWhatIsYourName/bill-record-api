package com.hwiyn.billrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Money {

    public static final String DEFAULT_CURRENCY = "CNY";
    private static final int MAX_PRECISION = 19;
    private static final int SCALE = 2;

    @Column(name = "amount", nullable = false, precision = MAX_PRECISION, scale = SCALE)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    public static Money of(BigDecimal amount) {
        return of(amount, DEFAULT_CURRENCY);
    }

    public static Money of(BigDecimal amount, String currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (amount.scale() > SCALE) {
            throw new IllegalArgumentException("Money amount scale must be less than or equal to 2");
        }

        BigDecimal normalized = amount.setScale(SCALE);
        if (normalized.precision() > MAX_PRECISION) {
            throw new IllegalArgumentException("Money amount must fit decimal(19,2)");
        }

        String normalizedCurrency = currency.trim().toUpperCase();
        if (normalizedCurrency.length() != 3) {
            throw new IllegalArgumentException("Currency must be an ISO-4217 code");
        }

        return new Money(normalized, normalizedCurrency);
    }
}
