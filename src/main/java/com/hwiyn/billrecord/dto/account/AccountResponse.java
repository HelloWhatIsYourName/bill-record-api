package com.hwiyn.billrecord.dto.account;

import com.hwiyn.billrecord.entity.AccountType;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        AccountType type,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        String currency,
        boolean archived) {
}
