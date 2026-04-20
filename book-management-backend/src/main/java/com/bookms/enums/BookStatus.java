package com.bookms.enums;

import lombok.Getter;

@Getter
public enum BookStatus {
    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final int code;

    private final String label;

    BookStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}