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
import com.bookms.security.SecurityUtils;
import com.bookms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final ReaderInfoRepository readerInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserVO> listUsers(String keyword, Integer status, Pageable pageable) {
        Page<SysUser> page = userRepository.findByConditions(keyword, status, pageable);
        return PageResponse.of(page.map(this::toUserVO));
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO createUser(UserCreateDTO dto) {
        if (userRepository.findByUsernameAndDeletedFalse(dto.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(UserStatus.ENABLED.getCode());

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<SysRole> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
        }
        userRepository.save(user);

        // If reader role assigned, create reader info
        boolean isReader = user.getRoles().stream()
                .anyMatch(r -> "reader".equals(r.getRoleCode()));
        if (isReader) {
            createReaderInfo(user.getId());
        }

        log.info("创建用户: {}", user.getUsername());
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(Long id, UserUpdateDTO dto) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());

        if (dto.getRoleIds() != null) {
            Set<SysRole> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
        }
        userRepository.save(user);
        return toUserVO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员账户");
        }
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
        log.info("重置用户密码: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return getUserById(userId);
    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = SecurityUtils.getCurrentUserId();
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserVO toUserVO(SysUser user) {
        List<UserVO.RoleVO> roleVOs = user.getRoles().stream()
                .map(r -> UserVO.RoleVO.builder()
                        .id(r.getId())
                        .roleCode(r.getRoleCode())
                        .roleName(r.getRoleName())
                        .build())
                .collect(Collectors.toList());

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roles(roleVOs)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private void createReaderInfo(Long userId) {
        if (readerInfoRepository.findByUserId(userId).isPresent()) return;

        ReaderInfo reader = new ReaderInfo();
        reader.setUserId(userId);
        reader.setReaderCardNo("RC" + System.currentTimeMillis() % 100000000L);
        reader.setReaderType("general");
        reader.setMaxBorrowCount(5);
        reader.setCurrentBorrowCount(0);
        reader.setCreditScore(100);
        reader.setIsBlacklist(false);
        reader.setValidDateStart(LocalDate.now());
        reader.setValidDateEnd(LocalDate.now().plusYears(4));
        readerInfoRepository.save(reader);
    }
}
