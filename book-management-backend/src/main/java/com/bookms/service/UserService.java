package com.bookms.service;

import com.bookms.dto.request.AssignRolesRequest;
import com.bookms.dto.request.UserCreateRequest;
import com.bookms.dto.request.UserUpdateRequest;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.UserResponse;

public interface UserService {

    PageResponse<UserResponse> pageUsers(String keyword, Short status, int page, int size);

    UserResponse getUser(Long id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserResponse assignRoles(Long id, AssignRolesRequest request);
}