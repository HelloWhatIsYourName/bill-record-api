package com.hwiyn.billrecord.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashFlowPointResponse(
        LocalDate date,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net) {
}
