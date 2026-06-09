package com.hwiyn.billrecord.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        String month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net,
        List<AccountBalanceResponse> accountBalances) {
}
