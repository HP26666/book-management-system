package com.bookms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private long expiresIn;

    private UserInfoResponse user;
}