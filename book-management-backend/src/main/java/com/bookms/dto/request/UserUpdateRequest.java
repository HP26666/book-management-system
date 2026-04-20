package com.bookms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Size(max = 50, message = "realName 长度不能超过 50")
    private String realName;

    @Pattern(regexp = "^1\\d{10}$", message = "phone 格式不正确")
    private String phone;

    @Email(message = "email 格式不正确")
    private String email;

    private String avatarUrl;

    private Short status;
}