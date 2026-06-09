package com.hwiyn.billrecord.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwiyn.billrecord.config.TimeConfig;
import com.hwiyn.billrecord.dto.budget.BudgetResponse;
import com.hwiyn.billrecord.dto.budget.CreateBudgetRequest;
import com.hwiyn.billrecord.dto.report.MonthlySummaryResponse;
import com.hwiyn.billrecord.exception.GlobalExceptionHandler;
import com.hwiyn.billrecord.security.JwtService;
import com.hwiyn.billrecord.security.UserPrincipal;
import com.hwiyn.billrecord.service.BudgetService;
import com.hwiyn.billrecord.service.ReportService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {BudgetController.class, ReportController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, TimeConfig.class})
class BudgetReportControllerTest {

    private static final UUID USER_ID = UUID.fromString("2e833469-a758-401a-9fc5-036ceaa67f50");
    private static final UUID BUDGET_ID = UUID.fromString("0b7683ee-49a2-45ff-96b5-1d11c9471a9f");
    private static final UUID CATEGORY_ID = UUID.fromString("a238c84b-10ad-4f05-8bfb-2be7c0ed7095");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtService jwtService;

    @Test
    void createsAndListsBudgets() throws Exception {
        BudgetResponse budget = budgetResponse();
        when(budgetService.create(eq(USER_ID), any(CreateBudgetRequest.class))).thenReturn(budget);
        when(budgetService.list(USER_ID, YearMonth.of(2026, 6))).thenReturn(List.of(budget));

        mockMvc.perform(post("/api/v1/budgets")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateBudgetRequest(
                                CATEGORY_ID,
                                "2026-06",
                                new BigDecimal("100.00"),
                                "CNY"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.remaining").value(70.00));

        mockMvc.perform(get("/api/v1/budgets")
                        .principal(authentication())
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(CATEGORY_ID.toString()));
    }

    @Test
    void returnsMonthlySummaryReport() throws Exception {
        when(reportService.monthlySummary(USER_ID, YearMonth.of(2026, 6))).thenReturn(new MonthlySummaryResponse(
                "2026-06",
                new BigDecimal("500.00"),
                new BigDecimal("80.00"),
                new BigDecimal("420.00"),
                List.of()));

        mockMvc.perform(get("/api/v1/reports/monthly-summary")
                        .principal(authentication())
                        .param("month", "2026-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.net").value(420.00));
    }

    private static BudgetResponse budgetResponse() {
        return new BudgetResponse(
                BUDGET_ID,
                CATEGORY_ID,
                "2026-06",
                new BigDecimal("100.00"),
                "CNY",
                new BigDecimal("30.00"),
                new BigDecimal("70.00"));
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user@example.com", List.of("USER"));
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
