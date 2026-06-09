package com.hwiyn.billrecord.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "0123456789012345678901234567890123456789012345678901234567890123";
    private static final Instant NOW = Instant.parse("2026-06-09T00:00:00Z");

    @Test
    void issuesAccessTokenWithRequiredClaims() {
        JwtService jwtService = new JwtService(jwtProperties(), fixedClock(NOW));
        UUID userId = UUID.fromString("8baf4161-134d-4f5f-9923-4b067364c3de");

        String token = jwtService.issueAccessToken(userId, "user@example.com", List.of("USER"));

        Claims claims = jwtService.parse(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getIssuer()).isEqualTo("bill-record-api");
        assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(claims.get("roles", List.class)).containsExactly("USER");
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(NOW);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plus(Duration.ofMinutes(30)));
    }

    @Test
    void rejectsExpiredToken() {
        JwtService issuer = new JwtService(jwtProperties(), fixedClock(NOW));
        String token = issuer.issueAccessToken(
                UUID.fromString("8baf4161-134d-4f5f-9923-4b067364c3de"),
                "user@example.com",
                List.of("USER"));
        JwtService parser = new JwtService(jwtProperties(), fixedClock(NOW.plus(Duration.ofMinutes(31))));

        assertThatThrownBy(() -> parser.parse(token))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("expired");
    }

    private static JwtProperties jwtProperties() {
        return new JwtProperties(
                "bill-record-api",
                SECRET,
                Duration.ofMinutes(30),
                Duration.ofDays(30));
    }

    private static Clock fixedClock(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }
}
