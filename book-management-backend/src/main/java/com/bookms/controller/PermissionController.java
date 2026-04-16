package com.bookms.controller;

import com.bookms.dto.ApiResponse;
import com.bookms.dto.PermissionTreeVO;
import com.bookms.security.SecurityUtils;
import com.bookms.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "权限树")
    @GetMapping("/tree")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<PermissionTreeVO>> getPermissionTree() {
        return ApiResponse.ok(permissionService.getPermissionTree());
    }

    @Operation(summary = "当前用户菜单")
    @GetMapping("/menus")
    public ApiResponse<List<PermissionTreeVO>> getUserMenus() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.ok(permissionService.getUserMenus(userId));
    }

    @Operation(summary = "当前用户权限码")
    @GetMapping("/codes")
    public ApiResponse<List<String>> getUserPermissionCodes() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.ok(permissionService.getUserPermissionCodes(userId));
    }
}
