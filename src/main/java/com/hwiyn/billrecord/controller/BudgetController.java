package com.hwiyn.billrecord.controller;

import com.hwiyn.billrecord.dto.budget.BudgetResponse;
import com.hwiyn.billrecord.dto.budget.CreateBudgetRequest;
import com.hwiyn.billrecord.dto.budget.UpdateBudgetRequest;
import com.hwiyn.billrecord.security.SecurityPrincipals;
import com.hwiyn.billrecord.service.BudgetService;
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
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create monthly budget")
    public BudgetResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateBudgetRequest request) {
        return budgetService.create(SecurityPrincipals.requireUserId(authentication), request);
    }

    @GetMapping
    @Operation(summary = "List monthly budgets")
    public List<BudgetResponse> list(Authentication authentication, @RequestParam String month) {
        return budgetService.list(SecurityPrincipals.requireUserId(authentication), BudgetService.parseMonth(month));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update budget")
    public BudgetResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        return budgetService.update(SecurityPrincipals.requireUserId(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete budget")
    public void delete(Authentication authentication, @PathVariable UUID id) {
        budgetService.delete(SecurityPrincipals.requireUserId(authentication), id);
    }
}
