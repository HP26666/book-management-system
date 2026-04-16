package com.bookms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reservation_record")
public class ReservationRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "reserve_date", nullable = false)
    private LocalDate reserveDate = LocalDate.now();

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "notify_at")
    private LocalDateTime notifyAt;
}
