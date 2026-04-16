package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.ReaderInfo;
import com.bookms.entity.SysRole;
import com.bookms.entity.SysUser;
import com.bookms.enums.UserStatus;
import com.bookms.exception.BusinessException;
import com.bookms.repository.ReaderInfoRepository;
import com.bookms.repository.SysRoleRepository;
import com.bookms.repository.SysUserRepository;
import com.bookms.security.JwtTokenProvider;
import com.bookms.security.UserPrincipal;
import com.bookms.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final ReaderInfoRepository readerInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${bookms.wechat.mock-enabled:true}")
    private boolean wechatMockEnabled;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildAuthResponse(principal);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsernameAndDeletedFalse(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        SysRole readerRole = roleRepository.findByRoleCodeAndDeletedFalse("reader")
                .orElseThrow(() -> new BusinessException("读者角色不存在"));

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.ENABLED.getCode());
        user.setRoles(Set.of(readerRole));
        userRepository.save(user);

        // Create reader info
        ReaderInfo readerInfo = new ReaderInfo();
        readerInfo.setUserId(user.getId());
        readerInfo.setReaderCardNo(generateReaderCardNo());
        readerInfo.setReaderType("general");
        readerInfo.setMaxBorrowCount(5);
        readerInfo.setCurrentBorrowCount(0);
        readerInfo.setCreditScore(100);
        readerInfo.setIsBlacklist(false);
        readerInfo.setValidDateStart(LocalDate.now());
        readerInfo.setValidDateEnd(LocalDate.now().plusYears(4));
        readerInfoRepository.save(readerInfo);

        log.info("用户注册成功: {}", user.getUsername());

        // Auto login
        List<String> roles = user.getRoles().stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), roles);
        return buildAuthResponse(principal);
    }

    @Override
    @Transactional
    public AuthResponse wechatLogin(WechatLoginRequest request) {
        if (!wechatMockEnabled) {
            throw new BusinessException("微信登录暂未开放");
        }
        // Mock mode: use code as username
        String mockUsername = "wx_" + request.getCode();
        SysUser user = userRepository.findByOpenidAndDeletedFalse(mockUsername)
                .orElseGet(() -> {
                    SysRole readerRole = roleRepository.findByRoleCodeAndDeletedFalse("reader")
                            .orElseThrow(() -> new BusinessException("读者角色不存在"));
                    SysUser newUser = new SysUser();
                    newUser.setUsername(mockUsername);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRealName("微信用户");
                    newUser.setOpenid(mockUsername);
                    newUser.setStatus(UserStatus.ENABLED.getCode());
                    newUser.setRoles(Set.of(readerRole));
                    return userRepository.save(newUser);
                });

        List<String> roles = user.getRoles().stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), roles);
        return buildAuthResponse(principal);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException("Refresh Token无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        String username = jwtTokenProvider.getUsernameFromToken(request.getRefreshToken());
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<String> roles = user.getRoles().stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), roles);
        return buildAuthResponse(principal);
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null && jwtTokenProvider.validateToken(token)) {
            long ttl = jwtTokenProvider.getExpirationMs(token);
            if (ttl > 0) {
                redisTemplate.opsForValue().set("token:blacklist:" + token, "1", ttl, TimeUnit.MILLISECONDS);
            }
        }
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                principal.getUserId(), principal.getUsername(), principal.getRoles());
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal.getUserId());

        SysUser user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        AuthResponse.UserInfoVO userInfo = AuthResponse.UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatarUrl(user.getAvatarUrl())
                .roles(principal.getRoles())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200)
                .user(userInfo)
                .build();
    }

    private String generateReaderCardNo() {
        return "RC" + System.currentTimeMillis() % 100000000L;
    }
}
