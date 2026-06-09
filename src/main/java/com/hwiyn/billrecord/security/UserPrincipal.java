package com.hwiyn.billrecord.security;

import java.util.List;
import java.util.UUID;

public record UserPrincipal(UUID id, String email, List<String> roles) {
}
