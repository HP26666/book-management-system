package com.bookms.controller;

import com.bookms.dto.*;
import com.bookms.security.JwtTokenProvider;
import com.bookms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("POST /auth/login - 成功")
    void login_success() throws Exception {
        AuthResponse.UserInfoVO userInfo = AuthResponse.UserInfoVO.builder()
                .id(1L)
                .username("admin")
                .realName("管理员")
                .roles(List.of("admin"))
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .expiresIn(7200)
                .user(userInfo)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    @DisplayName("POST /auth/register - 成功")
    void register_success() throws Exception {
        AuthResponse.UserInfoVO userInfo = AuthResponse.UserInfoVO.builder()
                .id(2L)
                .username("newuser")
                .roles(List.of("reader"))
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("new-token")
                .refreshToken("new-refresh")
                .expiresIn(7200)
                .user(userInfo)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("newpass123");
        req.setRealName("新用户");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("new-token"));
    }
}
