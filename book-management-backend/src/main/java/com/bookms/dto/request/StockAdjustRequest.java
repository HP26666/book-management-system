package com.bookms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustRequest {

    @NotNull(message = "changeQty 不能为空")
    private Integer changeQty;

    private String remark;
}