package com.hwiyn.billrecord.dto.category;

import com.hwiyn.billrecord.entity.CategoryType;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        String color,
        String icon,
        boolean archived) {
}
