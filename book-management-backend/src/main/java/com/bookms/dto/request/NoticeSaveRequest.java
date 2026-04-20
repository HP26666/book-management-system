package com.bookms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeSaveRequest {

    @NotBlank(message = "title 不能为空")
    @Size(max = 255, message = "title 长度不能超过 255")
    private String title;

    @NotBlank(message = "content 不能为空")
    private String content;

    @NotNull(message = "type 不能为空")
    private Short type;

    @NotNull(message = "status 不能为空")
    private Short status;

    private LocalDateTime publishTime;
}