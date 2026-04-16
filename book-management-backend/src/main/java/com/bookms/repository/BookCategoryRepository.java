package com.bookms.repository;

import com.bookms.entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    List<BookCategory> findByDeletedFalseOrderBySortOrder();

    List<BookCategory> findByParentIdAndDeletedFalse(Long parentId);
}
