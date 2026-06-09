package com.hwiyn.billrecord.dto.account;

import com.hwiyn.billrecord.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull AccountType type,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 17, fraction = 2) BigDecimal openingBalance,
        @Pattern(regexp = "^[A-Za-z]{3}$") String currency) {
}
