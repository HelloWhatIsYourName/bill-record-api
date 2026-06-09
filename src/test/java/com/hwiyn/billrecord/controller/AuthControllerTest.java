package com.hwiyn.billrecord.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwiyn.billrecord.dto.auth.AuthResponse;
import com.hwiyn.billrecord.dto.auth.LoginRequest;
import com.hwiyn.billrecord.dto.auth.RegisterRequest;
import com.hwiyn.billrecord.dto.user.UserResponse;
import com.hwiyn.billrecord.config.TimeConfig;
import com.hwiyn.billrecord.exception.GlobalExceptionHandler;
import com.hwiyn.billrecord.security.JwtService;
import com.hwiyn.billrecord.security.UserPrincipal;
import com.hwiyn.billrecord.service.AuthService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AuthController.class, UserController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, TimeConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void registerReturnsCreatedAuthResponse() throws Exception {
        UserResponse user = userResponse();
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse(user));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "user@example.com",
                                "Str0ngPass!2026",
                                "Demo User"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    void loginReturnsAuthResponse() throws Exception {
        UserResponse user = userResponse();
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse(user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "user@example.com",
                                "Str0ngPass!2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.displayName").value("Demo User"));
    }

    @Test
    void validationErrorsUseApiErrorShape() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void currentUserReturnsProfileForAuthenticatedPrincipal() throws Exception {
        UserResponse user = userResponse();
        UUID userId = user.id();
        when(authService.currentUser(userId)).thenReturn(user);

        UserPrincipal principal = new UserPrincipal(userId, "user@example.com", List.of("USER"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(get("/api/v1/users/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.defaultCurrency").value("CNY"));
    }

    private static AuthResponse authResponse(UserResponse user) {
        return new AuthResponse(
                "access-token",
                "Bearer",
                Instant.parse("2026-06-09T00:30:00Z"),
                user);
    }

    private static UserResponse userResponse() {
        return new UserResponse(
                UUID.fromString("8baf4161-134d-4f5f-9923-4b067364c3de"),
                "user@example.com",
                "Demo User",
                "CNY");
    }
}
