package com.hwiyn.billrecord.dto.category;

import com.hwiyn.billrecord.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull CategoryType type,
        @Size(max = 20) String color,
        @Size(max = 40) String icon) {
}
