package com.hwiyn.billrecord.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            authenticate(authorization.substring(BEARER_PREFIX.length()));
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private void authenticate(String token) {
        try {
            Claims claims = jwtService.parse(token);
            List<String> roles = claims.get("roles", List.class);
            UserPrincipal principal = new UserPrincipal(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    roles);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, token, authorities));
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
    }
}
