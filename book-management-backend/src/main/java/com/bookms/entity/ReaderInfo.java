package com.bookms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reader_info")
public class ReaderInfo extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reader_card_no", length = 20)
    private String readerCardNo;

    @Column(name = "reader_type", nullable = false, length = 20)
    private String readerType = "general";

    @Column(name = "max_borrow_count", nullable = false)
    private Integer maxBorrowCount = 5;

    @Column(name = "current_borrow_count", nullable = false)
    private Integer currentBorrowCount = 0;

    @Column(name = "credit_score", nullable = false)
    private Integer creditScore = 100;

    @Column(name = "is_blacklist", nullable = false)
    private Boolean isBlacklist = false;

    @Column(name = "valid_date_start")
    private LocalDate validDateStart;

    @Column(name = "valid_date_end")
    private LocalDate validDateEnd;
}
