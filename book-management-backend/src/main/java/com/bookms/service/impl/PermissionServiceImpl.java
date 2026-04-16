package com.bookms.service.impl;

import com.bookms.dto.PermissionTreeVO;
import com.bookms.entity.SysPermission;
import com.bookms.entity.SysRole;
import com.bookms.entity.SysUser;
import com.bookms.repository.SysPermissionRepository;
import com.bookms.repository.SysUserRepository;
import com.bookms.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionRepository permissionRepository;
    private final SysUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionTreeVO> getPermissionTree() {
        List<SysPermission> all = permissionRepository.findByDeletedFalseOrderBySortOrder();
        return buildTree(all, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionTreeVO> getUserMenus(Long userId) {
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        Set<SysPermission> permissions = new HashSet<>();
        for (SysRole role : user.getRoles()) {
            permissions.addAll(role.getPermissions());
        }

        List<SysPermission> menus = permissions.stream()
                .filter(p -> "menu".equals(p.getType()))
                .sorted(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0))
                .collect(Collectors.toList());

        return buildTree(menus, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserPermissionCodes(Long userId) {
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        Set<String> codes = new HashSet<>();
        for (SysRole role : user.getRoles()) {
            for (SysPermission perm : role.getPermissions()) {
                if (perm.getPermCode() != null) {
                    codes.add(perm.getPermCode());
                }
            }
        }
        return new ArrayList<>(codes);
    }

    private List<PermissionTreeVO> buildTree(List<SysPermission> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> Objects.equals(p.getParentId(), parentId))
                .map(p -> PermissionTreeVO.builder()
                        .id(p.getId())
                        .permName(p.getPermName())
                        .permCode(p.getPermCode())
                        .type(p.getType())
                        .path(p.getPath())
                        .icon(p.getIcon())
                        .sortOrder(p.getSortOrder())
                        .parentId(p.getParentId())
                        .children(buildTree(permissions, p.getId()))
                        .build())
                .collect(Collectors.toList());
    }
}
