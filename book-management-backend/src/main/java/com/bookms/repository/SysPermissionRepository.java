package com.bookms.repository;

import com.bookms.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {
    List<SysPermission> findByDeletedFalseOrderBySortOrder();

    List<SysPermission> findByParentIdAndDeletedFalseOrderBySortOrder(Long parentId);
}
