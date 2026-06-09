package com.hwiyn.billrecord.dto.budget;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetRequest(
        @NotNull UUID categoryId,
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$") String month,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "^[A-Za-z]{3}$") String currency) {
}
