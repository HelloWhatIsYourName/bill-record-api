package com.hwiyn.billrecord.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hwiyn.billrecord.controller.UserController;
import com.hwiyn.billrecord.exception.GlobalExceptionHandler;
import com.hwiyn.billrecord.security.JwtAuthenticationFilter;
import com.hwiyn.billrecord.security.JwtService;
import com.hwiyn.billrecord.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, TimeConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void protectedRoutesReturnJsonUnauthorizedErrorWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }
}
