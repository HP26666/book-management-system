package com.bookms.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationResponse {

    private Long id;

    private Long userId;

    private String username;

    private String realName;

    private Long bookId;

    private String bookTitle;

    private Integer status;

    private LocalDate reserveDate;

    private LocalDate expireDate;

    private LocalDateTime notifyAt;
}