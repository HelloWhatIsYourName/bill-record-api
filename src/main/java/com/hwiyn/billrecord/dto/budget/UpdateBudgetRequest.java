package com.hwiyn.billrecord.dto.budget;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateBudgetRequest(
        @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "^[A-Za-z]{3}$") String currency) {
}
