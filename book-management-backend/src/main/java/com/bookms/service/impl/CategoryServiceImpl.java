package com.bookms.service.impl;

import com.bookms.dto.CategoryDTO;
import com.bookms.dto.CategoryTreeVO;
import com.bookms.entity.BookCategory;
import com.bookms.exception.BusinessException;
import com.bookms.repository.BookCategoryRepository;
import com.bookms.repository.BookInfoRepository;
import com.bookms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final BookCategoryRepository categoryRepository;
    private final BookInfoRepository bookInfoRepository;

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        List<BookCategory> all = categoryRepository.findByDeletedFalseOrderBySortOrder();
        return buildTree(all, 0L);
    }

    @Override
    @Transactional
    public CategoryTreeVO createCategory(CategoryDTO dto) {
        BookCategory category = new BookCategory();
        category.setName(dto.getName());
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setLevel(dto.getParentId() != null && dto.getParentId() > 0 ? 2 : 1);
        categoryRepository.save(category);
        return toCategoryTreeVO(category);
    }

    @Override
    @Transactional
    public CategoryTreeVO updateCategory(Long id, CategoryDTO dto) {
        BookCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
        if (dto.getName() != null) category.setName(dto.getName());
        if (dto.getParentId() != null) category.setParentId(dto.getParentId());
        if (dto.getSortOrder() != null) category.setSortOrder(dto.getSortOrder());
        categoryRepository.save(category);
        return toCategoryTreeVO(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        BookCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        List<BookCategory> children = categoryRepository.findByParentIdAndDeletedFalse(id);
        if (!children.isEmpty()) {
            throw new BusinessException("存在子分类，无法删除");
        }

        long bookCount = bookInfoRepository.countByCategoryIdAndDeletedFalse(id);
        if (bookCount > 0) {
            throw new BusinessException("该分类下有图书，无法删除");
        }

        category.setDeleted(true);
        categoryRepository.save(category);
    }

    private List<CategoryTreeVO> buildTree(List<BookCategory> categories, Long parentId) {
        return categories.stream()
                .filter(c -> Objects.equals(c.getParentId(), parentId))
                .map(c -> {
                    CategoryTreeVO vo = toCategoryTreeVO(c);
                    vo.setChildren(buildTree(categories, c.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private CategoryTreeVO toCategoryTreeVO(BookCategory c) {
        return CategoryTreeVO.builder()
                .id(c.getId())
                .name(c.getName())
                .parentId(c.getParentId())
                .sortOrder(c.getSortOrder())
                .build();
    }
}
