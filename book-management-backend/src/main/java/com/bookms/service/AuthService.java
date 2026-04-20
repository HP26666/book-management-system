package com.bookms.service;

import com.bookms.dto.request.LoginRequest;
import com.bookms.dto.request.RefreshTokenRequest;
import com.bookms.dto.request.RegisterRequest;
import com.bookms.dto.request.WechatLoginRequest;
import com.bookms.dto.response.LoginResponse;
import com.bookms.dto.response.RefreshTokenResponse;
import com.bookms.dto.response.RegisterResponse;
import com.bookms.dto.response.UserInfoResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    void logout(String accessToken);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    UserInfoResponse currentUser();

    LoginResponse wechatLogin(WechatLoginRequest request);
}