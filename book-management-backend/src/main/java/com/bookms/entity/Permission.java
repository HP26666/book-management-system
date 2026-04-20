package com.bookms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_permission")
public class Permission extends BaseEntity {

    @Column(name = "perm_code", nullable = false, unique = true, length = 100)
    private String permCode;

    @Column(name = "perm_name", nullable = false, length = 100)
    private String permName;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(length = 255)
    private String path;

    @Column(length = 100)
    private String icon;

    @Column(nullable = false)
    private Short status = 1;
}