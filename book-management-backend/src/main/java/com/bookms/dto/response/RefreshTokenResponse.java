package com.bookms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenResponse {

    private String accessToken;

    private long expiresIn;
}