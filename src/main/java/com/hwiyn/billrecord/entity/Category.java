package com.hwiyn.billrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Category {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    @Column(length = 20)
    private String color;

    @Column(length = 40)
    private String icon;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Category create(
            UUID userId,
            String name,
            CategoryType type,
            String color,
            String icon,
            Instant now) {
        return new Category(
                UUID.randomUUID(),
                userId,
                name.trim(),
                type,
                blankToNull(color),
                blankToNull(icon),
                false,
                now,
                now);
    }

    public void update(String name, String color, String icon, Instant now) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        this.color = blankToNull(color);
        this.icon = blankToNull(icon);
        this.updatedAt = now;
    }

    public void archive(Instant now) {
        this.archived = true;
        this.updatedAt = now;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
