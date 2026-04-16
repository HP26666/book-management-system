package com.bookms.enums;

import lombok.Getter;

@Getter
public enum PermissionType {
    MENU("menu"),
    BUTTON("button"),
    API("api");

    private final String code;

    PermissionType(String code) {
        this.code = code;
    }
}
