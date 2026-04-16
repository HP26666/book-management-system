package com.bookms.repository;

import com.bookms.entity.SysOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysOperationLogRepository extends JpaRepository<SysOperationLog, Long> {
    Page<SysOperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
