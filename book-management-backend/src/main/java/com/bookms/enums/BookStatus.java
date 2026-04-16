package com.bookms.enums;

import lombok.Getter;

@Getter
public enum BookStatus {
    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");

    private final int code;
    private final String desc;

    BookStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
