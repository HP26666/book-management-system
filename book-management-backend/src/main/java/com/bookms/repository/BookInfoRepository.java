package com.bookms.repository;

import com.bookms.entity.BookInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {

    Optional<BookInfo> findByIdAndDeletedFalse(Long id);

    Optional<BookInfo> findByIsbnAndDeletedFalse(String isbn);

    @Query("SELECT b FROM BookInfo b WHERE b.deleted = false " +
            "AND (:keyword IS NULL OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) " +
            "AND (:categoryId IS NULL OR b.categoryId = :categoryId) " +
            "AND (:status IS NULL OR b.status = :status)")
    Page<BookInfo> findByConditions(@Param("keyword") String keyword,
                          @Param("categoryId") Long categoryId,
                          @Param("status") Integer status,
                          Pageable pageable);

    @Modifying
    @Query("UPDATE BookInfo b SET b.availableStock = b.availableStock - 1 " +
            "WHERE b.id = :bookId AND b.availableStock > 0 AND b.deleted = false")
    int decrementStock(@Param("bookId") Long bookId);

    @Modifying
    @Query("UPDATE BookInfo b SET b.availableStock = b.availableStock + 1 " +
            "WHERE b.id = :bookId AND b.availableStock < b.totalStock AND b.deleted = false")
    int incrementStock(@Param("bookId") Long bookId);

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(Integer status);

    long countByCategoryIdAndDeletedFalse(Long categoryId);

    @Query("SELECT b FROM BookInfo b WHERE b.deleted = false AND b.status = 1 ORDER BY b.createdAt DESC")
    List<BookInfo> findLatestBooks(Pageable pageable);
}
