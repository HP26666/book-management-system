package com.bookms.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDTO {
    private String realName;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer status;
    private List<Long> roleIds;
}
