package com.bookms.repository;

import com.bookms.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {
    List<SysRole> findByDeletedFalse();

    Optional<SysRole> findByRoleCodeAndDeletedFalse(String roleCode);

    Optional<SysRole> findByIdAndDeletedFalse(Long id);
}
