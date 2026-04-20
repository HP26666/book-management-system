package com.bookms.repository;

import com.bookms.entity.ReservationRecord;
import com.bookms.enums.ReservationStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<ReservationRecord, Long>, JpaSpecificationExecutor<ReservationRecord> {

    @EntityGraph(attributePaths = {"user", "book", "book.category"})
    Optional<ReservationRecord> findByIdAndDeletedFalse(Long id);

    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<ReservationStatus> statuses);

    boolean existsByBookIdAndStatusIn(Long bookId, Collection<ReservationStatus> statuses);

    @EntityGraph(attributePaths = {"user", "book"})
    Optional<ReservationRecord> findFirstByBookIdAndStatusAndExpireDateGreaterThanEqualOrderByReserveDateAsc(
            Long bookId,
            ReservationStatus status,
            LocalDate expireDate);

    long countByStatus(ReservationStatus status);

    @Query("select rr from ReservationRecord rr where rr.status in :activeStatuses and rr.expireDate < :today")
    List<ReservationRecord> findExpiredReservations(@Param("today") LocalDate today, @Param("activeStatuses") Collection<ReservationStatus> activeStatuses);
}