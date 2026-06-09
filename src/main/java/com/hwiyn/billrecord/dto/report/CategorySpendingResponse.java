package com.hwiyn.billrecord.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySpendingResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal amount,
        String currency) {
}
