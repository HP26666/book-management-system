package com.bookms.repository;

import com.bookms.entity.ReservationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRecordRepository extends JpaRepository<ReservationRecord, Long> {

    @Query("SELECT r FROM ReservationRecord r WHERE r.deleted = false " +
            "AND (:keyword IS NULL OR CAST(r.bookId AS string) LIKE %:keyword%) " +
            "AND (:status IS NULL OR r.status = :status)")
    Page<ReservationRecord> findByConditions(@Param("keyword") String keyword,
                                             @Param("status") Integer status,
                                             Pageable pageable);

    Page<ReservationRecord> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    Page<ReservationRecord> findByUserIdAndStatusAndDeletedFalse(Long userId, Integer status, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ReservationRecord r " +
            "WHERE r.userId = :userId AND r.bookId = :bookId AND r.status = 0 AND r.deleted = false")
    boolean existsActiveReservation(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Query("SELECT r FROM ReservationRecord r WHERE r.bookId = :bookId AND r.status = 0 AND r.deleted = false " +
            "ORDER BY r.reserveDate ASC, r.id ASC")
    List<ReservationRecord> findQueueByBookId(@Param("bookId") Long bookId);

    List<ReservationRecord> findByStatusAndExpireDateBeforeAndDeletedFalse(Integer status, LocalDate date);
}
