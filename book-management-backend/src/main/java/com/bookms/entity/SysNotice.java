package com.bookms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_notice")
public class SysNotice extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 20)
    private String type = "notice";

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "publisher_id")
    private Long publisherId;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;
}
