package com.bookms.service.impl;

import com.bookms.dto.RoleDTO;
import com.bookms.entity.SysPermission;
import com.bookms.entity.SysRole;
import com.bookms.exception.BusinessException;
import com.bookms.repository.SysPermissionRepository;
import com.bookms.repository.SysRoleRepository;
import com.bookms.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.Hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SysRole> listAllRoles() {
        List<SysRole> roles = roleRepository.findAll();
        for (SysRole role : roles) {
            Hibernate.initialize(role.getPermissions());
        }
        return roles;
    }

    @Override
    @Transactional(readOnly = true)
    public SysRole getRoleById(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        Hibernate.initialize(role.getPermissions());
        return role;
    }

    @Override
    @Transactional
    public SysRole createRole(RoleDTO dto) {
        if (roleRepository.findByRoleCodeAndDeletedFalse(dto.getRoleCode()).isPresent()) {
            throw new BusinessException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());

        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            Set<SysPermission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
            role.setPermissions(permissions);
        }
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public SysRole updateRole(Long id, RoleDTO dto) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if (dto.getRoleName() != null) role.setRoleName(dto.getRoleName());
        if (dto.getDescription() != null) role.setDescription(dto.getDescription());

        if (dto.getPermissionIds() != null) {
            Set<SysPermission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
            role.setPermissions(permissions);
        }
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if ("admin".equals(role.getRoleCode())) {
            throw new BusinessException("不能删除管理员角色");
        }
        role.setDeleted(true);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        Set<SysPermission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}
