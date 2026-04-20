package com.bookms.service.impl;

import com.bookms.dto.request.AssignRolesRequest;
import com.bookms.dto.request.UserCreateRequest;
import com.bookms.dto.request.UserUpdateRequest;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.UserResponse;
import com.bookms.entity.Role;
import com.bookms.entity.User;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.UserMapper;
import com.bookms.repository.RoleRepository;
import com.bookms.repository.UserRepository;
import com.bookms.service.UserService;
import com.bookms.util.PageableUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> pageUsers(String keyword, Short status, int page, int size) {
        Pageable pageable = PageableUtils.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserResponse> result = userRepository.findAll(buildSpecification(keyword, status), pageable)
                .map(userMapper::toResponse);
        return PageResponse.of(result);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userMapper.toResponse(getUserEntity(id));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhoneAndDeletedFalse(request.getPhone())) {
            throw new BusinessException(409, "手机号已存在");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus() == null ? (short) 1 : request.getStatus());
        user.setRoles(resolveRoles(request.getRoleIds()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = getUserEntity(id);
        if (StringUtils.hasText(request.getPhone())
                && userRepository.existsByPhoneAndDeletedFalseAndIdNot(request.getPhone(), id)) {
            throw new BusinessException(409, "手机号已存在");
        }
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(getUserEntity(id));
    }

    @Override
    @Transactional
    public UserResponse assignRoles(Long id, AssignRolesRequest request) {
        User user = getUserEntity(id);
        user.setRoles(resolveRoles(request.getRoleIds()));
        return userMapper.toResponse(userRepository.save(user));
    }

    private User getUserEntity(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private LinkedHashSet<Role> resolveRoles(List<Long> roleIds) {
        List<Long> safeRoleIds = roleIds == null || roleIds.isEmpty()
                ? roleRepository.findByRoleCode("reader").map(role -> List.of(role.getId())).orElse(List.of())
                : roleIds;
        List<Role> roles = roleRepository.findAllById(safeRoleIds);
        if (roles.size() != safeRoleIds.size()) {
            throw new BusinessException(400, "存在无效角色");
        }
        return new LinkedHashSet<>(roles);
    }

    private Specification<User> buildSpecification(String keyword, Short status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("realName")), like)));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}