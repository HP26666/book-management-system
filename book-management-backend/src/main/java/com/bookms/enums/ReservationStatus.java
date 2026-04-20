package com.bookms.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    RESERVED(0, "预约中"),
    NOTIFIED(1, "已通知"),
    CANCELLED(2, "已取消"),
    EXPIRED(3, "已过期");

    private final int code;

    private final String label;

    ReservationStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}