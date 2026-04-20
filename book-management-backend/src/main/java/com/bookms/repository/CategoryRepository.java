package com.bookms.repository;

import com.bookms.entity.Category;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    List<Category> findAllByOrderByLevelAscSortOrderAscIdAsc();

    boolean existsByNameAndDeletedFalse(String name);

    boolean existsByParentIdAndDeletedFalse(Long parentId);

    boolean existsByParentId(Long parentId);

    Page<Category> findByParentId(Long parentId, Pageable pageable);

    Page<Category> findByParentIsNull(Pageable pageable);
}