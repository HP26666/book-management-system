package com.bookms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustDTO {
    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能为负")
    private Integer totalStock;
    private String remark;
}
