package com.bookms.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    WAITING(0, "预约中"),
    NOTIFIED(1, "已通知"),
    CANCELLED(2, "已取消"),
    EXPIRED(3, "已过期");

    private final int code;
    private final String desc;

    ReservationStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReservationStatus fromCode(int code) {
        for (ReservationStatus s : values()) {
            if (s.code == code) return s;
        }
        return WAITING;
    }
}
