package com.bookms.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatarUrl;

    private Short status;

    private LocalDateTime lastLoginAt;

    private List<RoleResponse> roles;
}