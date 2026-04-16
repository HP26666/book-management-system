package com.bookms.repository;

import com.bookms.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    @Query("SELECT br FROM BorrowRecord br WHERE br.deleted = false " +
            "AND (:keyword IS NULL OR br.borrowNo LIKE %:keyword%) " +
            "AND (:status IS NULL OR br.status = :status)")
    Page<BorrowRecord> findByConditions(@Param("keyword") String keyword,
                                        @Param("status") Integer status,
                                        Pageable pageable);

    Page<BorrowRecord> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    Page<BorrowRecord> findByUserIdAndStatusAndDeletedFalse(Long userId, Integer status, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(br) > 0 THEN true ELSE false END FROM BorrowRecord br " +
            "WHERE br.userId = :userId AND br.bookId = :bookId AND br.status IN (0, 1, 2) AND br.deleted = false")
    boolean existsActiveBorrow(@Param("userId") Long userId, @Param("bookId") Long bookId);

    long countByStatusAndDeletedFalse(Integer status);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.deleted = false " +
            "AND br.status IN (2, 3, 5) AND br.borrowDate >= :startDate")
    long countByBorrowDateAfter(@Param("startDate") LocalDate startDate);

    @Query("SELECT br.bookId, COUNT(br) AS cnt FROM BorrowRecord br " +
            "WHERE br.deleted = false AND br.status IN (2, 3, 5) " +
            "GROUP BY br.bookId ORDER BY cnt DESC")
    List<Object[]> findTopBorrowedBooks(Pageable pageable);

    @Query("SELECT YEAR(br.borrowDate), MONTH(br.borrowDate), COUNT(br) " +
            "FROM BorrowRecord br WHERE br.deleted = false AND br.status IN (2, 3, 5) " +
            "AND br.borrowDate >= :startDate AND br.borrowDate <= :endDate " +
            "GROUP BY YEAR(br.borrowDate), MONTH(br.borrowDate) " +
            "ORDER BY YEAR(br.borrowDate), MONTH(br.borrowDate)")
    List<Object[]> monthlyBorrowTrend(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    List<BorrowRecord> findByStatusAndDueDateBeforeAndDeletedFalse(Integer status, LocalDate date);
}
