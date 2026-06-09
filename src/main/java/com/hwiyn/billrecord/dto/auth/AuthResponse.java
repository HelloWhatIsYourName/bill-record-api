package com.hwiyn.billrecord.dto.auth;

import com.hwiyn.billrecord.dto.user.UserResponse;
import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserResponse user) {
}
