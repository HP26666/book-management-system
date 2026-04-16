package com.bookms.repository;

import com.bookms.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsernameAndDeletedFalse(String username);

    Optional<SysUser> findByOpenidAndDeletedFalse(String openid);

    boolean existsByUsernameAndDeletedFalse(String username);

    @Query("SELECT u FROM SysUser u WHERE u.deleted = false " +
            "AND (:keyword IS NULL OR u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.phone LIKE %:keyword%) " +
            "AND (:status IS NULL OR u.status = :status)")
    Page<SysUser> findByConditions(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    Optional<SysUser> findByIdAndDeletedFalse(Long id);
}
