package com.bookms.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowApplyRequest {

    @NotNull(message = "bookId 不能为空")
    private Long bookId;

    @NotNull(message = "borrowDays 不能为空")
    @Min(value = 1, message = "borrowDays 不能小于 1")
    @Max(value = 90, message = "borrowDays 不能大于 90")
    private Integer borrowDays;
}