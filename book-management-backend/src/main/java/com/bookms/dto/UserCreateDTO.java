package com.bookms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50)
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100)
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer status = 1;
    private List<Long> roleIds;
}
