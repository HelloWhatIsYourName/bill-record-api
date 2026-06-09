package com.hwiyn.billrecord.dto.transaction;

import com.hwiyn.billrecord.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        UUID accountId,
        UUID transferAccountId,
        UUID categoryId,
        BigDecimal amount,
        String currency,
        Instant transactionTime,
        String note) {
}
