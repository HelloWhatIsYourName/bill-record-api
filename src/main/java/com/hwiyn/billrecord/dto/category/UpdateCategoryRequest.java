package com.hwiyn.billrecord.dto.category;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(max = 80) String name,
        @Size(max = 20) String color,
        @Size(max = 40) String icon) {
}
