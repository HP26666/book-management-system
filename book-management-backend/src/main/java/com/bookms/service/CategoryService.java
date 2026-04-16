package com.bookms.service;

import com.bookms.dto.CategoryDTO;
import com.bookms.dto.CategoryTreeVO;

import java.util.List;

public interface CategoryService {
    List<CategoryTreeVO> getCategoryTree();
    CategoryTreeVO createCategory(CategoryDTO dto);
    CategoryTreeVO updateCategory(Long id, CategoryDTO dto);
    void deleteCategory(Long id);
}
