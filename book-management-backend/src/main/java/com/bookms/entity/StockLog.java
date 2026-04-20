package com.bookms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "book_stock_log")
public class StockLog extends CreatedOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @Column(name = "change_qty", nullable = false)
    private Integer changeQty;

    @Column(name = "before_qty", nullable = false)
    private Integer beforeQty;

    @Column(name = "after_qty", nullable = false)
    private Integer afterQty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "related_borrow_id")
    private Long relatedBorrowId;

    @Column(name = "related_reservation_id")
    private Long relatedReservationId;

    @Column(length = 255)
    private String remark;
}