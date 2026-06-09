package com.hwiyn.billrecord.security;

import com.hwiyn.billrecord.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

public final class SecurityPrincipals {

    private SecurityPrincipals() {
    }

    public static UUID requireUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.id();
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required");
    }
}
