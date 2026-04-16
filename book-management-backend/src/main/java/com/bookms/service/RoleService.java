package com.bookms.service;

import com.bookms.dto.RoleDTO;
import com.bookms.entity.SysRole;

import java.util.List;

public interface RoleService {
    List<SysRole> listAllRoles();
    SysRole getRoleById(Long id);
    SysRole createRole(RoleDTO dto);
    SysRole updateRole(Long id, RoleDTO dto);
    void deleteRole(Long id);
    void assignPermissions(Long roleId, List<Long> permissionIds);
}
