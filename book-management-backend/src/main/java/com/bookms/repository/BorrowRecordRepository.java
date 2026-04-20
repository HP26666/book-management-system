package com.bookms.repository;

import com.bookms.entity.BorrowRecord;
import com.bookms.enums.BorrowStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {

    @EntityGraph(attributePaths = {"user", "book", "book.category", "approveUser"})
    Optional<BorrowRecord> findByIdAndDeletedFalse(Long id);

    long countByUserIdAndStatusIn(Long userId, Collection<BorrowStatus> statuses);

    boolean existsByBookIdAndStatusIn(Long bookId, Collection<BorrowStatus> statuses);

    long countByStatus(BorrowStatus status);

    List<BorrowRecord> findTop5ByStatusOrderByCreatedAtDesc(BorrowStatus status);

    @Query(value = "select coalesce(max(cast(right(borrow_no, 4) as integer)), 0) from borrow_record where borrow_no like concat(:prefix, '%')", nativeQuery = true)
    Integer findMaxSequenceByBorrowNoPrefix(@Param("prefix") String prefix);

    @Query("select count(br) from BorrowRecord br where br.createdAt >= :start and br.createdAt < :end")
    long countCreatedBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("select br from BorrowRecord br where br.dueDate < :today and br.status in :statuses")
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today, @Param("statuses") Collection<BorrowStatus> statuses);

    @Query("select br.book.id, br.book.title, count(br.id) from BorrowRecord br group by br.book.id, br.book.title order by count(br.id) desc")
    List<Object[]> findPopularBooks(Pageable pageable);
}