package com.hwiyn.billrecord.service;

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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final CategoryService categoryService;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper,
            CategoryService categoryService,
            JwtProperties jwtProperties,
            Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.categoryService = categoryService;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email already exists");
        }

        Instant now = Instant.now(clock);
        User user = User.create(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                now);
        User savedUser = userRepository.save(user);
        categoryService.seedDefaultsForUser(savedUser.getId(), now);

        return authResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .filter(candidate -> candidate.isEnabled())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isEnabled)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
    }

    private AuthResponse authResponse(User user) {
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getEmail(), List.of(user.getRole()));
        return new AuthResponse(
                accessToken,
                TOKEN_TYPE,
                Instant.now(clock).plus(jwtProperties.accessTokenTtl()),
                userMapper.toResponse(user));
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
