package com.bookms.repository;

import com.bookms.entity.ReaderInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReaderInfoRepository extends JpaRepository<ReaderInfo, Long> {
    Optional<ReaderInfo> findByUserId(Long userId);

    @Query("SELECT r FROM ReaderInfo r WHERE r.deleted = false " +
            "AND (:keyword IS NULL OR r.readerCardNo LIKE %:keyword%)")
    Page<ReaderInfo> findByConditions(@Param("keyword") String keyword, Pageable pageable);
}
