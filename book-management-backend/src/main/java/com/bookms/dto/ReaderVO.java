package com.bookms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderVO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String readerCardNo;
    private String readerType;
    private Integer maxBorrowCount;
    private Integer currentBorrowCount;
    private Integer creditScore;
    private Boolean isBlacklist;
    private LocalDate validDateStart;
    private LocalDate validDateEnd;
    private LocalDateTime createdAt;
}
