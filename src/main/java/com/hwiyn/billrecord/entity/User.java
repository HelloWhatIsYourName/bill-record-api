package com.hwiyn.billrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    public static final String ROLE_USER = "USER";

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static User create(String email, String passwordHash, String displayName, Instant now) {
        return new User(
                UUID.randomUUID(),
                email.toLowerCase(),
                passwordHash,
                displayName,
                Money.DEFAULT_CURRENCY,
                ROLE_USER,
                true,
                now,
                now);
    }

    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = Instant.now();
    }
}
