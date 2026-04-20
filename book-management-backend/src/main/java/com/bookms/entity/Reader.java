package com.bookms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "reader_info")
@SQLDelete(sql = "UPDATE reader_info SET deleted = true, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class Reader extends SoftDeleteEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "reader_card_no", length = 32, unique = true)
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
    private Boolean blacklist = Boolean.FALSE;

    @Column(name = "valid_date_start")
    private LocalDate validDateStart;

    @Column(name = "valid_date_end")
    private LocalDate validDateEnd;
}