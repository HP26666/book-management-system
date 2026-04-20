package com.bookms.enums;

import lombok.Getter;

@Getter
public enum NoticeType {
    INFO(1, "info"),
    SUCCESS(2, "success"),
    WARN(3, "warn");

    private final int code;

    private final String label;

    NoticeType(int code, String label) {
        this.code = code;
        this.label = label;
    }
}