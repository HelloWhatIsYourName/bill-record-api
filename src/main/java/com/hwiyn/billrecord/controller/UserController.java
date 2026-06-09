package com.hwiyn.billrecord.controller;

import com.hwiyn.billrecord.dto.user.UserResponse;
import com.hwiyn.billrecord.security.SecurityPrincipals;
import com.hwiyn.billrecord.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public UserResponse currentUser(Authentication authentication) {
        return authService.currentUser(SecurityPrincipals.requireUserId(authentication));
    }
}
