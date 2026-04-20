package com.bookms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WechatLoginRequest {

    @NotBlank(message = "code 不能为空")
    private String code;

    private String encryptedData;

    private String iv;

    private String phoneCode;
}