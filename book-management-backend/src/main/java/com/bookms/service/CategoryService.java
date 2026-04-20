package com.bookms.service;

import com.bookms.dto.request.CategorySaveRequest;
import com.bookms.dto.response.CategoryResponse;
import com.bookms.dto.response.PageResponse;
import java.util.List;

public interface CategoryService {

    List<CategoryResponse> tree();

    PageResponse<CategoryResponse> pageCategories(Long parentId, int page, int size);

    CategoryResponse create(CategorySaveRequest request);

    CategoryResponse update(Long id, CategorySaveRequest request);

    void delete(Long id);
}