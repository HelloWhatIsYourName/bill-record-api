package com.hwiyn.billrecord.dto.transaction;

import com.hwiyn.billrecord.entity.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull TransactionType type,
        @NotNull UUID accountId,
        UUID transferAccountId,
        UUID categoryId,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
        @NotNull Instant transactionTime,
        @Size(max = 500) String note) {
}
