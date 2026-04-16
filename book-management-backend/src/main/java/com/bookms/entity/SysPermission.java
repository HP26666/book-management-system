package com.bookms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_permission")
public class SysPermission extends BaseEntity {

    @Column(name = "perm_code", nullable = false, length = 100)
    private String permCode;

    @Column(name = "perm_name", nullable = false, length = 50)
    private String permName;

    @Column(nullable = false, length = 20)
    private String type = "menu";

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(length = 200)
    private String path;

    @Column(length = 100)
    private String icon;

    @Column(nullable = false)
    private Integer status = 1;
}
