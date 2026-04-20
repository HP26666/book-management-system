package com.bookms.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeResponse {

    private Long id;

    private String title;

    private String content;

    private Short type;

    private Short status;

    private Long publisherId;

    private String publisherName;

    private LocalDateTime publishTime;
}