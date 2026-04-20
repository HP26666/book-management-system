package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.AssignRolesRequest;
import com.bookms.dto.request.UserCreateRequest;
import com.bookms.dto.request.UserUpdateRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.UserResponse;
import com.bookms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> pageUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Short status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.pageUsers(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @OperationLog("创建用户")
    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success("创建成功", userService.createUser(request));
    }

    @OperationLog("更新用户")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success("更新成功", userService.updateUser(id, request));
    }

    @OperationLog("删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }

    @OperationLog("分配用户角色")
    @PutMapping("/{id}/roles")
    public ApiResponse<UserResponse> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesRequest request) {
        return ApiResponse.success("分配成功", userService.assignRoles(id, request));
    }
}