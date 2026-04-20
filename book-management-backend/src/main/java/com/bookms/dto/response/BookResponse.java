package com.bookms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookResponse {

    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    private Long categoryId;

    private String categoryName;

    private String coverUrl;

    private String summary;

    private BigDecimal price;

    private Integer totalStock;

    private Integer availableStock;

    private Integer status;

    private String location;

    private LocalDateTime createdAt;
}