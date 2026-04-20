package com.bookms.service.impl;

import com.bookms.dto.request.CategorySaveRequest;
import com.bookms.dto.response.CategoryResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.entity.Category;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.CategoryMapper;
import com.bookms.repository.BookRepository;
import com.bookms.repository.CategoryRepository;
import com.bookms.service.CategoryService;
import com.bookms.util.PageableUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> tree() {
        List<Category> categories = categoryRepository.findAllByOrderByLevelAscSortOrderAscIdAsc();
        Map<Long, CategoryResponse> responseMap = new LinkedHashMap<>();
        List<CategoryResponse> roots = new ArrayList<>();

        for (Category category : categories) {
            responseMap.put(category.getId(), categoryMapper.toResponse(category));
        }
        for (Category category : categories) {
            CategoryResponse response = responseMap.get(category.getId());
            if (category.getParent() == null) {
                roots.add(response);
            } else {
                CategoryResponse parent = responseMap.get(category.getParent().getId());
                if (parent != null) {
                    parent.getChildren().add(response);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> pageCategories(Long parentId, int page, int size) {
        Pageable pageable = PageableUtils.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by("id")));
        Page<CategoryResponse> result = (parentId == null
                        ? categoryRepository.findByParentIsNull(pageable)
                        : categoryRepository.findByParentId(parentId, pageable))
                .map(categoryMapper::toResponse);
        return PageResponse.of(result);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategorySaveRequest request) {
        Category category = categoryMapper.toEntity(request);
        Category parent = resolveParent(request.getParentId());
        category.setParent(parent);
        category.setLevel(parent == null ? 1 : parent.getLevel() + 1);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategorySaveRequest request) {
        Category category = getCategory(id);
        Category parent = resolveParent(request.getParentId());
        if (parent != null && parent.getId().equals(id)) {
            throw new BusinessException(400, "分类不能设置自己为父级");
        }
        categoryMapper.updateEntity(request, category);
        category.setParent(parent);
        category.setLevel(parent == null ? 1 : parent.getLevel() + 1);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = getCategory(id);
        if (categoryRepository.existsByParentIdAndDeletedFalse(id)) {
            throw new BusinessException(409, "存在子分类，无法删除");
        }
        if (bookRepository.existsByCategoryIdAndDeletedFalse(id)) {
            throw new BusinessException(409, "分类下存在图书，无法删除");
        }
        categoryRepository.delete(category);
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return getCategory(parentId);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new BusinessException(404, "分类不存在"));
    }
}