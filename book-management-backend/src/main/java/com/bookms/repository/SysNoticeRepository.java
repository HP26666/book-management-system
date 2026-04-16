package com.bookms.repository;

import com.bookms.entity.SysNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SysNoticeRepository extends JpaRepository<SysNotice, Long> {

    @Query("SELECT n FROM SysNotice n WHERE n.deleted = false " +
            "AND (:keyword IS NULL OR n.title LIKE %:keyword%)")
    Page<SysNotice> findByConditions(@Param("keyword") String keyword, Pageable pageable);

    List<SysNotice> findByStatusAndDeletedFalseOrderByCreatedAtDesc(Integer status, Pageable pageable);
}
