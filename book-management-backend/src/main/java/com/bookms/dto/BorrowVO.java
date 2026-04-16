package com.bookms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowVO {
    private Long id;
    private String borrowNo;
    private Long userId;
    private String username;
    private String realName;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Integer status;
    private String statusDesc;
    private Integer renewCount;
    private BigDecimal fineAmount;
    private Long approveUserId;
    private String approveUsername;
    private LocalDateTime approveTime;
    private String rejectReason;
    private String remark;
    private LocalDateTime createdAt;
}
