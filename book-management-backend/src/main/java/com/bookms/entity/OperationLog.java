package com.bookms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_operation_log")
public class OperationLog extends CreatedOnlyEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String operation;

    @Column(length = 255)
    private String method;

    @Column(name = "request_uri", length = 255)
    private String requestUri;

    @Column(columnDefinition = "TEXT")
    private String params;

    @Column(length = 50)
    private String ip;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(nullable = false)
    private Short status = 1;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;
}