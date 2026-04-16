package com.bookms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private Long parentId = 0L;
    private Integer sortOrder = 0;
}
