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
public class ReservationVO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Integer status;
    private String statusDesc;
    private LocalDate reserveDate;
    private LocalDate expireDate;
    private LocalDateTime notifyAt;
    private LocalDateTime createdAt;
}
