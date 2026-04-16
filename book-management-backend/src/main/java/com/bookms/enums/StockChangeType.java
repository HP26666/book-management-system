package com.bookms.enums;

import lombok.Getter;

@Getter
public enum StockChangeType {
    INIT("init", "初始化"),
    BORROW("borrow", "借出"),
    RETURN("return", "归还"),
    ADJUST("adjust", "人工调整");

    private final String code;
    private final String desc;

    StockChangeType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
