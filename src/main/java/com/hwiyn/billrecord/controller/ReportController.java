package com.hwiyn.billrecord.controller;

import com.hwiyn.billrecord.dto.report.CashFlowPointResponse;
import com.hwiyn.billrecord.dto.report.CategorySpendingResponse;
import com.hwiyn.billrecord.dto.report.MonthlySummaryResponse;
import com.hwiyn.billrecord.security.SecurityPrincipals;
import com.hwiyn.billrecord.service.BudgetService;
import com.hwiyn.billrecord.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Get monthly summary")
    public MonthlySummaryResponse monthlySummary(Authentication authentication, @RequestParam String month) {
        return reportService.monthlySummary(
                SecurityPrincipals.requireUserId(authentication),
                BudgetService.parseMonth(month));
    }

    @GetMapping("/category-spending")
    @Operation(summary = "Get category spending")
    public List<CategorySpendingResponse> categorySpending(
            Authentication authentication,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return reportService.categorySpending(SecurityPrincipals.requireUserId(authentication), from, to);
    }

    @GetMapping("/cash-flow")
    @Operation(summary = "Get cash flow")
    public List<CashFlowPointResponse> cashFlow(
            Authentication authentication,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return reportService.cashFlow(SecurityPrincipals.requireUserId(authentication), from, to);
    }
}
