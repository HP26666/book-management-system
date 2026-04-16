package com.bookms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionTreeVO {
    private Long id;
    private String permCode;
    private String permName;
    private String type;
    private Long parentId;
    private Integer sortOrder;
    private String path;
    private String icon;
    private Integer status;
    private List<PermissionTreeVO> children;
}
