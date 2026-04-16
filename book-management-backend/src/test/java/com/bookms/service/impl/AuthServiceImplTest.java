package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.*;
import com.bookms.enums.UserStatus;
import com.bookms.exception.BusinessException;
import com.bookms.repository.*;
import com.bookms.security.JwtTokenProvider;
import com.bookms.security.UserPrincipal;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private SysRoleRepository roleRepository;

    @Mock
    private ReaderInfoRepository readerInfoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private StringRedisTemplate redisTemplate;

    private SysUser sampleUser;
    private SysRole readerRole;

    @BeforeEach
    void setUp() {
        readerRole = new SysRole();
        readerRole.setId(3L);
        readerRole.setRoleCode("reader");
        readerRole.setRoleName("读者");

        sampleUser = new SysUser();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setPassword("$2a$10$encodedPassword");
        sampleUser.setRealName("测试用户");
        sampleUser.setStatus(UserStatus.ENABLED.getCode());
        sampleUser.setRoles(Set.of(readerRole));
        sampleUser.setDeleted(false);
    }

    @Test
    @DisplayName("登录 - 成功")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");

        UserPrincipal principal = new UserPrincipal(1L, "testuser", List.of("reader"));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenProvider.generateAccessToken(eq(1L), eq("testuser"), anyList())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        AuthResponse result = authService.login(req);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getExpiresIn()).isEqualTo(7200);
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("登录 - 用户不存在")
    void login_userNotFound() {
        LoginRequest req = new LoginRequest();
        req.setUsername("nonexistent");
        req.setPassword("password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("用户名或密码错误"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("登录 - 密码错误")
    void login_wrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("用户名或密码错误"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("登录 - 账号已禁用")
    void login_accountDisabled() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("账号已禁用"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    @DisplayName("注册 - 成功")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("newpass123");
        req.setRealName("新用户");
        req.setPhone("13800138000");

        when(userRepository.findByUsernameAndDeletedFalse("newuser")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleCodeAndDeletedFalse("reader")).thenReturn(Optional.of(readerRole));
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(SysUser.class))).thenAnswer(i -> {
            SysUser u = i.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtTokenProvider.generateAccessToken(eq(2L), eq("newuser"), anyList())).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(2L)).thenReturn("refresh");
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        AuthResponse result = authService.register(req);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("token");
        verify(readerInfoRepository).save(any(ReaderInfo.class));
    }

    @Test
    @DisplayName("注册 - 用户名已存在")
    void register_duplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setPassword("password");

        when(userRepository.findByUsernameAndDeletedFalse("testuser")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class);
    }
}
