package com.hwiyn.billrecord.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceResponse(
        UUID accountId,
        String name,
        BigDecimal balance,
        String currency) {
}
