package com.hwiyn.billrecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hwiyn.billrecord.dto.auth.AuthResponse;
import com.hwiyn.billrecord.dto.auth.LoginRequest;
import com.hwiyn.billrecord.dto.auth.RegisterRequest;
import com.hwiyn.billrecord.dto.user.UserResponse;
import com.hwiyn.billrecord.entity.User;
import com.hwiyn.billrecord.exception.ApiException;
import com.hwiyn.billrecord.mapper.UserMapper;
import com.hwiyn.billrecord.repository.UserRepository;
import com.hwiyn.billrecord.security.JwtProperties;
import com.hwiyn.billrecord.security.JwtService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-09T00:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CategoryService categoryService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                userMapper,
                categoryService,
                jwtProperties(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void registersUserWithLowercaseEmailAndReturnsAccessToken() {
        RegisterRequest request = new RegisterRequest("USER@Example.COM", "Str0ngPass!2026", "Demo User");
        UserResponse userResponse = new UserResponse(
                UUID.fromString("8baf4161-134d-4f5f-9923-4b067364c3de"),
                "user@example.com",
                "Demo User",
                "CNY");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Str0ngPass!2026")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.issueAccessToken(any(UUID.class), eq("user@example.com"), eq(List.of("USER"))))
                .thenReturn("access-token");
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(30)));
        assertThat(response.user()).isEqualTo(userResponse);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(userCaptor.getValue().getDefaultCurrency()).isEqualTo("CNY");
        verify(categoryService).seedDefaultsForUser(userCaptor.getValue().getId(), NOW);
    }

    @Test
    void rejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Str0ngPass!2026", "Demo User");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
                });
    }

    @Test
    void logsInWithValidCredentials() {
        User user = User.create("user@example.com", "bcrypt-hash", "Demo User", NOW);
        LoginRequest request = new LoginRequest("user@example.com", "Str0ngPass!2026");
        UserResponse userResponse = new UserResponse(user.getId(), "user@example.com", "Demo User", "CNY");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Str0ngPass!2026", "bcrypt-hash")).thenReturn(true);
        when(jwtService.issueAccessToken(user.getId(), "user@example.com", List.of("USER"))).thenReturn("access-token");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user()).isEqualTo(userResponse);
    }

    @Test
    void rejectsInvalidPassword() {
        User user = User.create("user@example.com", "bcrypt-hash", "Demo User", NOW);
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(exception.getCode()).isEqualTo("INVALID_CREDENTIALS");
                });
    }

    private static JwtProperties jwtProperties() {
        return new JwtProperties(
                "bill-record-api",
                "0123456789012345678901234567890123456789012345678901234567890123",
                Duration.ofMinutes(30),
                Duration.ofDays(30));
    }
}
