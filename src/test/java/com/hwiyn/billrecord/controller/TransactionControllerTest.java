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
import com.hwiyn.billrecord.dto.transaction.CreateTransactionRequest;
import com.hwiyn.billrecord.dto.transaction.TransactionResponse;
import com.hwiyn.billrecord.entity.TransactionType;
import com.hwiyn.billrecord.exception.GlobalExceptionHandler;
import com.hwiyn.billrecord.security.JwtService;
import com.hwiyn.billrecord.security.UserPrincipal;
import com.hwiyn.billrecord.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
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

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, TimeConfig.class})
class TransactionControllerTest {

    private static final UUID USER_ID = UUID.fromString("2e833469-a758-401a-9fc5-036ceaa67f50");
    private static final UUID ACCOUNT_ID = UUID.fromString("4c3cdd0d-ad71-4be7-a3d8-b151b6bf94bc");
    private static final UUID CATEGORY_ID = UUID.fromString("a238c84b-10ad-4f05-8bfb-2be7c0ed7095");
    private static final UUID TRANSACTION_ID = UUID.fromString("a91ae013-6fc0-459d-a109-58328dd09ab3");
    private static final Instant TRANSACTION_TIME = Instant.parse("2026-06-09T08:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    @Test
    void createsAndListsTransactions() throws Exception {
        TransactionResponse transaction = transactionResponse();
        when(transactionService.create(eq(USER_ID), any(CreateTransactionRequest.class))).thenReturn(transaction);
        when(transactionService.list(eq(USER_ID), eq(TransactionType.EXPENSE), eq(ACCOUNT_ID), eq(CATEGORY_ID)))
                .thenReturn(List.of(transaction));

        mockMvc.perform(post("/api/v1/transactions")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTransactionRequest(
                                TransactionType.EXPENSE,
                                ACCOUNT_ID,
                                null,
                                CATEGORY_ID,
                                new BigDecimal("25.50"),
                                "CNY",
                                TRANSACTION_TIME,
                                "Lunch"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(25.50));

        mockMvc.perform(get("/api/v1/transactions")
                        .principal(authentication())
                        .param("type", "EXPENSE")
                        .param("accountId", ACCOUNT_ID.toString())
                        .param("categoryId", CATEGORY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TRANSACTION_ID.toString()));
    }

    @Test
    void deletesTransaction() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/{id}", TRANSACTION_ID).principal(authentication()))
                .andExpect(status().isNoContent());
    }

    private static TransactionResponse transactionResponse() {
        return new TransactionResponse(
                TRANSACTION_ID,
                TransactionType.EXPENSE,
                ACCOUNT_ID,
                null,
                CATEGORY_ID,
                new BigDecimal("25.50"),
                "CNY",
                TRANSACTION_TIME,
                "Lunch");
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user@example.com", List.of("USER"));
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
