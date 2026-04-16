package com.bookms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowApplyDTO {
    @NotNull(message = "图书ID不能为空")
    private Long bookId;
    private Integer borrowDays = 30;
}
