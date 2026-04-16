package com.bookms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String type = "notice";
    private Integer status = 1;
}
