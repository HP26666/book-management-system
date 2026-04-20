package com.bookms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRolesRequest {

    @NotEmpty(message = "roleIds 不能为空")
    private List<Long> roleIds;
}