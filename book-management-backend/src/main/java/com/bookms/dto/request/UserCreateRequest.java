package com.bookms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "username 不能为空")
    @Size(min = 4, max = 50, message = "username 长度必须在 4 到 50 之间")
    private String username;

    @NotBlank(message = "password 不能为空")
    @Size(min = 6, max = 50, message = "password 长度必须在 6 到 50 之间")
    private String password;

    @Size(max = 50, message = "realName 长度不能超过 50")
    private String realName;

    @Pattern(regexp = "^1\\d{10}$", message = "phone 格式不正确")
    private String phone;

    @Email(message = "email 格式不正确")
    private String email;

    private String avatarUrl;

    private Short status;

    private List<Long> roleIds;
}