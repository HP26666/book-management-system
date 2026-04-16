package com.bookms.controller;

import com.bookms.aspect.OperationLog;
import com.bookms.dto.ApiResponse;
import com.bookms.dto.CategoryDTO;
import com.bookms.dto.CategoryTreeVO;
import com.bookms.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类树")
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeVO>> getCategoryTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    @Operation(summary = "创建分类")
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("创建分类")
    public ApiResponse<CategoryTreeVO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        return ApiResponse.ok(categoryService.createCategory(dto));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("更新分类")
    public ApiResponse<CategoryTreeVO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return ApiResponse.ok(categoryService.updateCategory(id, dto));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("删除分类")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok(null);
    }
}
