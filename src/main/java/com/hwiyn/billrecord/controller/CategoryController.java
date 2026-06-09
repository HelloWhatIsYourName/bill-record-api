package com.hwiyn.billrecord.controller;

import com.hwiyn.billrecord.dto.category.CategoryResponse;
import com.hwiyn.billrecord.dto.category.CreateCategoryRequest;
import com.hwiyn.billrecord.dto.category.UpdateCategoryRequest;
import com.hwiyn.billrecord.entity.CategoryType;
import com.hwiyn.billrecord.security.SecurityPrincipals;
import com.hwiyn.billrecord.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category")
    public CategoryResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(SecurityPrincipals.requireUserId(authentication), request);
    }

    @GetMapping
    @Operation(summary = "List categories")
    public List<CategoryResponse> list(Authentication authentication, @RequestParam(required = false) CategoryType type) {
        return categoryService.list(SecurityPrincipals.requireUserId(authentication), type);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category")
    public CategoryResponse get(Authentication authentication, @PathVariable UUID id) {
        return categoryService.get(SecurityPrincipals.requireUserId(authentication), id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update category metadata")
    public CategoryResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.update(SecurityPrincipals.requireUserId(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive category")
    public void archive(Authentication authentication, @PathVariable UUID id) {
        categoryService.archive(SecurityPrincipals.requireUserId(authentication), id);
    }
}
