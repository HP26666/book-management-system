package com.bookms.dto;

import lombok.Data;

@Data
public class WechatLoginRequest {
    private String code;
    private String encryptedData;
    private String iv;
    private String phoneCode;
    // Mock模式下直接作为用户标识
    private String nickName;
    private String avatarUrl;
}
