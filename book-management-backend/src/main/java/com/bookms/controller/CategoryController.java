package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.CategorySaveRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.CategoryResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public ApiResponse<List<CategoryResponse>> tree() {
        return ApiResponse.success(categoryService.tree());
    }

    @GetMapping
    public ApiResponse<PageResponse<CategoryResponse>> pageCategories(
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(categoryService.pageCategories(parentId, page, size));
    }

    @OperationLog("创建分类")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success("创建成功", categoryService.create(request));
    }

    @OperationLog("更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success("更新成功", categoryService.update(id, request));
    }

    @OperationLog("删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}