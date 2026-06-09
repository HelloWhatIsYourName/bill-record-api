package com.hwiyn.billrecord.controller;

import com.hwiyn.billrecord.dto.transaction.CreateTransactionRequest;
import com.hwiyn.billrecord.dto.transaction.TransactionResponse;
import com.hwiyn.billrecord.dto.transaction.UpdateTransactionRequest;
import com.hwiyn.billrecord.entity.TransactionType;
import com.hwiyn.billrecord.security.SecurityPrincipals;
import com.hwiyn.billrecord.service.TransactionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create transaction")
    public TransactionResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.create(SecurityPrincipals.requireUserId(authentication), request);
    }

    @GetMapping
    @Operation(summary = "List transactions")
    public List<TransactionResponse> list(
            Authentication authentication,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId) {
        return transactionService.list(SecurityPrincipals.requireUserId(authentication), type, accountId, categoryId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction")
    public TransactionResponse get(Authentication authentication, @PathVariable UUID id) {
        return transactionService.get(SecurityPrincipals.requireUserId(authentication), id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update transaction")
    public TransactionResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return transactionService.update(SecurityPrincipals.requireUserId(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete transaction")
    public void delete(Authentication authentication, @PathVariable UUID id) {
        transactionService.delete(SecurityPrincipals.requireUserId(authentication), id);
    }
}
