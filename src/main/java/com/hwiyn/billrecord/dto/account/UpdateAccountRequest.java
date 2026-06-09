package com.hwiyn.billrecord.dto.account;

import com.hwiyn.billrecord.entity.AccountType;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @Size(max = 80) String name,
        AccountType type) {
}
