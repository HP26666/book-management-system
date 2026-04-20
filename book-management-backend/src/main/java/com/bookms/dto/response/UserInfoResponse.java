package com.bookms.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatarUrl;

    private List<String> roles;

    private List<String> permissions;
}