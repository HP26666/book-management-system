package com.bookms.dto.response;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReaderResponse {

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
}