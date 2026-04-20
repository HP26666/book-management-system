package com.bookms.service.impl;

import com.bookms.dto.request.LoginRequest;
import com.bookms.dto.request.RefreshTokenRequest;
import com.bookms.dto.request.RegisterRequest;
import com.bookms.dto.request.WechatLoginRequest;
import com.bookms.dto.response.LoginResponse;
import com.bookms.dto.response.RefreshTokenResponse;
import com.bookms.dto.response.RegisterResponse;
import com.bookms.dto.response.UserInfoResponse;
import com.bookms.entity.Role;
import com.bookms.entity.User;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.UserMapper;
import com.bookms.repository.RoleRepository;
import com.bookms.repository.UserRepository;
import com.bookms.security.CustomUserPrincipal;
import com.bookms.security.JwtTokenProvider;
import com.bookms.security.SecurityUtils;
import com.bookms.security.TokenBlacklistService;
import com.bookms.service.AuthService;
import com.bookms.service.ReaderService;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final ReaderService readerService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletedFalse(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        CustomUserPrincipal principal = CustomUserPrincipal.fromUser(saved);
        return buildLoginResponse(principal, userMapper.toUserInfo(saved));
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhoneAndDeletedFalse(request.getPhone())) {
            throw new BusinessException(409, "手机号已存在");
        }

        Role readerRole = roleRepository.findByRoleCode("reader")
                .orElseThrow(() -> new BusinessException(500, "读者角色不存在"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus((short) 1);
        user.setRoles(new LinkedHashSet<>(java.util.Set.of(readerRole)));

        User saved = userRepository.save(user);
        readerService.createDefaultReader(saved);
        return new RegisterResponse(saved.getId(), saved.getUsername());
    }

    @Override
    public void logout(String accessToken) {
        jwtTokenProvider.validateToken(accessToken);
        tokenBlacklistService.blacklist(accessToken, jwtTokenProvider.getExpiration(accessToken));
    }

    @Override
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        jwtTokenProvider.validateToken(refreshToken);
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(401, "refreshToken 无效");
        }
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用");
        }
        String accessToken = jwtTokenProvider.createAccessToken(CustomUserPrincipal.fromUser(user));
        return new RefreshTokenResponse(accessToken, jwtTokenProvider.getAccessTokenExpirationSeconds());
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse currentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return userMapper.toUserInfo(user);
    }

    @Override
    @Transactional
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        String normalizedCode = request.getCode().replaceAll("[^0-9A-Za-z_-]", "");
        if (!StringUtils.hasText(normalizedCode)) {
            normalizedCode = "guest";
        }
        String generatedUsername = ("wx_mock_" + normalizedCode).toLowerCase(Locale.ROOT);
        if (generatedUsername.length() > 50) {
            generatedUsername = generatedUsername.substring(0, 50);
        }
        final String username = generatedUsername;

        User user = userRepository.findByUsernameAndDeletedFalse(username).orElseGet(() -> {
            Role readerRole = roleRepository.findByRoleCode("reader")
                    .orElseThrow(() -> new BusinessException(500, "读者角色不存在"));
            User created = new User();
            created.setUsername(username);
            created.setPassword(passwordEncoder.encode("mock-wechat-user"));
            created.setRealName("微信用户");
            created.setStatus((short) 1);
            created.setRoles(new LinkedHashSet<>(java.util.Set.of(readerRole)));
            User saved = userRepository.save(created);
            readerService.createDefaultReader(saved);
            return saved;
        });

        user.setLastLoginAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        CustomUserPrincipal principal = CustomUserPrincipal.fromUser(saved);
        return buildLoginResponse(principal, userMapper.toUserInfo(saved));
    }

    private LoginResponse buildLoginResponse(CustomUserPrincipal principal, UserInfoResponse userInfo) {
        return new LoginResponse(
                jwtTokenProvider.createAccessToken(principal),
                jwtTokenProvider.createRefreshToken(principal),
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                userInfo);
    }
}