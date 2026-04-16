package com.bookms.enums;

import lombok.Getter;

@Getter
public enum BorrowStatus {
    PENDING(0, "申请中"),
    APPROVED(1, "已通过/待取书"),
    BORROWED(2, "借阅中"),
    RETURNED(3, "已归还"),
    REJECTED(4, "已拒绝"),
    OVERDUE(5, "逾期");

    private final int code;
    private final String desc;

    BorrowStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BorrowStatus fromCode(int code) {
        for (BorrowStatus s : values()) {
            if (s.code == code) return s;
        }
        return PENDING;
    }
}
