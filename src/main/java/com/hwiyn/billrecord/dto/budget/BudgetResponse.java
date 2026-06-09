package com.hwiyn.billrecord.dto.budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        String month,
        BigDecimal amount,
        String currency,
        BigDecimal spent,
        BigDecimal remaining) {
}
