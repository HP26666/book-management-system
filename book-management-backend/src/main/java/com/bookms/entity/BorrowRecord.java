package com.bookms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "borrow_record")
public class BorrowRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "borrow_no", nullable = false, length = 20)
    private String borrowNo;

    @Column(name = "borrow_date")
    private LocalDate borrowDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "renew_count", nullable = false)
    private Integer renewCount = 0;

    @Column(name = "fine_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "approve_user_id")
    private Long approveUserId;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(length = 500)
    private String remark;
}
