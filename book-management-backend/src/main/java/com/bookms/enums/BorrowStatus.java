package com.bookms.enums;

import lombok.Getter;

@Getter
public enum BorrowStatus {
    APPLYING(0, "申请中"),
    APPROVED_PENDING_PICKUP(1, "已通过待取书"),
    BORROWING(2, "借阅中"),
    RETURNED(3, "已归还"),
    REJECTED(4, "已拒绝"),
    OVERDUE(5, "逾期");

    private final int code;

    private final String label;

    BorrowStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}