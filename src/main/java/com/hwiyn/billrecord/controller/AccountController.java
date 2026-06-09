package com.hwiyn.billrecord.controller;

import com.hwiyn.billrecord.dto.account.AccountResponse;
import com.hwiyn.billrecord.dto.account.CreateAccountRequest;
import com.hwiyn.billrecord.dto.account.UpdateAccountRequest;
import com.hwiyn.billrecord.security.SecurityPrincipals;
import com.hwiyn.billrecord.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create account")
    public AccountResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(SecurityPrincipals.requireUserId(authentication), request);
    }

    @GetMapping
    @Operation(summary = "List active accounts")
    public List<AccountResponse> list(Authentication authentication) {
        return accountService.listActive(SecurityPrincipals.requireUserId(authentication));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account")
    public AccountResponse get(Authentication authentication, @PathVariable UUID id) {
        return accountService.get(SecurityPrincipals.requireUserId(authentication), id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update account metadata")
    public AccountResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.update(SecurityPrincipals.requireUserId(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive account")
    public void archive(Authentication authentication, @PathVariable UUID id) {
        accountService.archive(SecurityPrincipals.requireUserId(authentication), id);
    }
}
