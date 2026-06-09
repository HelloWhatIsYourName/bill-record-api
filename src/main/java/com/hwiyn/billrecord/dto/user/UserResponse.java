package com.hwiyn.billrecord.dto.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String defaultCurrency) {
}
