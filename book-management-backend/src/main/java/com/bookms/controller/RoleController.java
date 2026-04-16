package com.bookms.controller;

import com.bookms.aspect.OperationLog;
import com.bookms.dto.ApiResponse;
import com.bookms.dto.RoleDTO;
import com.bookms.entity.SysRole;
import com.bookms.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表")
    @GetMapping
    public ApiResponse<List<SysRole>> listRoles() {
        return ApiResponse.ok(roleService.listAllRoles());
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    public ApiResponse<SysRole> getRoleById(@PathVariable Long id) {
        return ApiResponse.ok(roleService.getRoleById(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @OperationLog("创建角色")
    public ApiResponse<SysRole> createRole(@Valid @RequestBody RoleDTO dto) {
        return ApiResponse.ok(roleService.createRole(dto));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @OperationLog("更新角色")
    public ApiResponse<SysRole> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        return ApiResponse.ok(roleService.updateRole(id, dto));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @OperationLog("删除角色")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "分配权限")
    @PostMapping("/{id}/permissions")
    @OperationLog("分配权限")
    public ApiResponse<Void> assignPermissions(
            @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ApiResponse.ok(null);
    }
}
