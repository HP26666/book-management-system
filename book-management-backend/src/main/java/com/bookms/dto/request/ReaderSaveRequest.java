package com.bookms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReaderSaveRequest {

    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotBlank(message = "readerType 不能为空")
    private String readerType;

    @NotNull(message = "maxBorrowCount 不能为空")
    @Min(value = 1, message = "maxBorrowCount 不能小于 1")
    private Integer maxBorrowCount;

    private Boolean isBlacklist;

    private LocalDate validDateStart;

    private LocalDate validDateEnd;
}