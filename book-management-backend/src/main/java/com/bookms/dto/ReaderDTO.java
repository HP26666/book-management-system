package com.bookms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReaderDTO {
    private Long userId;
    private String readerType = "general";
    private Integer maxBorrowCount = 5;
    private LocalDate validDateStart;
    private LocalDate validDateEnd;
}
