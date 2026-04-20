package com.bookms.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IsbnMetadataResponse {

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private String summary;

    private BigDecimal price;
}