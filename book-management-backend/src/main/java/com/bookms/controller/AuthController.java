package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.LoginRequest;
import com.bookms.dto.request.RefreshTokenRequest;
import com.bookms.dto.request.RegisterRequest;
import com.bookms.dto.request.WechatLoginRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.LoginResponse;
import com.bookms.dto.response.RefreshTokenResponse;
import com.bookms.dto.response.RegisterResponse;
import com.bookms.dto.response.UserInfoResponse;
import com.bookms.exception.BusinessException;
import com.bookms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @OperationLog("用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @OperationLog("用户注册")
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @OperationLog("用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(400, "Authorization 头格式不正确");
        }
        authService.logout(authorization.substring(7));
        return ApiResponse.success("退出成功", null);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("刷新成功", authService.refresh(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me() {
        return ApiResponse.success(authService.currentUser());
    }

    @OperationLog("微信登录")
    @PostMapping("/wechat-login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success("登录成功", authService.wechatLogin(request));
    }
}