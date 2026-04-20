package com.bookms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowResponse {

    private Long id;

    private String borrowNo;

    private Long userId;

    private String username;

    private String realName;

    private Long bookId;

    private String bookTitle;

    private String coverUrl;

    private LocalDate borrowDate;

    private LocalDate dueDate;

    private LocalDate returnDate;

    private Integer status;

    private Integer renewCount;

    private BigDecimal fineAmount;

    private Long approveUserId;

    private String approveUserName;

    private LocalDateTime approveTime;

    private String rejectReason;

    private String remark;
}