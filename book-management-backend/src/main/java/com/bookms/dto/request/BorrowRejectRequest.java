package com.bookms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowRejectRequest {

    @NotBlank(message = "rejectReason 不能为空")
    @Size(max = 500, message = "rejectReason 长度不能超过 500")
    private String rejectReason;
}