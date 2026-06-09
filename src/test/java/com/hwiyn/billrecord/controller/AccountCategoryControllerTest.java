package com.hwiyn.billrecord.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwiyn.billrecord.config.TimeConfig;
import com.hwiyn.billrecord.dto.account.AccountResponse;
import com.hwiyn.billrecord.dto.account.CreateAccountRequest;
import com.hwiyn.billrecord.dto.category.CategoryResponse;
import com.hwiyn.billrecord.dto.category.CreateCategoryRequest;
import com.hwiyn.billrecord.entity.AccountType;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.exception.GlobalExceptionHandler;
import com.hwiyn.billrecord.security.JwtService;
import com.hwiyn.billrecord.security.UserPrincipal;
import com.hwiyn.billrecord.service.AccountService;
import com.hwiyn.billrecord.service.CategoryService;
import java.math.BigDecimal;
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

@WebMvcTest(controllers = {AccountController.class, CategoryController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, TimeConfig.class})
class AccountCategoryControllerTest {

    private static final UUID USER_ID = UUID.fromString("2e833469-a758-401a-9fc5-036ceaa67f50");
    private static final UUID RESOURCE_ID = UUID.fromString("4c3cdd0d-ad71-4be7-a3d8-b151b6bf94bc");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @Test
    void createsAndListsAccounts() throws Exception {
        AccountResponse account = new AccountResponse(
                RESOURCE_ID,
                "现金钱包",
                AccountType.CASH,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "CNY",
                false);
        when(accountService.create(eq(USER_ID), any(CreateAccountRequest.class))).thenReturn(account);
        when(accountService.listActive(USER_ID)).thenReturn(List.of(account));

        mockMvc.perform(post("/api/v1/accounts")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccountRequest(
                                "现金钱包",
                                AccountType.CASH,
                                new BigDecimal("100.00"),
                                "CNY"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentBalance").value(100.00));

        mockMvc.perform(get("/api/v1/accounts").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("现金钱包"));
    }

    @Test
    void archivesAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/{id}", RESOURCE_ID).principal(authentication()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createsAndListsCategories() throws Exception {
        CategoryResponse category = new CategoryResponse(
                RESOURCE_ID,
                "餐饮",
                CategoryType.EXPENSE,
                "#EF4444",
                "utensils",
                false);
        when(categoryService.create(eq(USER_ID), any(CreateCategoryRequest.class))).thenReturn(category);
        when(categoryService.list(USER_ID, CategoryType.EXPENSE)).thenReturn(List.of(category));

        mockMvc.perform(post("/api/v1/categories")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCategoryRequest(
                                "餐饮",
                                CategoryType.EXPENSE,
                                "#EF4444",
                                "utensils"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EXPENSE"));

        mockMvc.perform(get("/api/v1/categories")
                        .param("type", "EXPENSE")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("餐饮"));
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user@example.com", List.of("USER"));
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
