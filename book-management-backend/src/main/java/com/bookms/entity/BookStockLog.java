package com.bookms.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "book_stock_log")
public class BookStockLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @Column(name = "change_qty", nullable = false)
    private Integer changeQty;

    @Column(name = "before_qty", nullable = false)
    private Integer beforeQty;

    @Column(name = "after_qty", nullable = false)
    private Integer afterQty;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "related_borrow_id")
    private Long relatedBorrowId;

    @Column(name = "related_reservation_id")
    private Long relatedReservationId;

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
