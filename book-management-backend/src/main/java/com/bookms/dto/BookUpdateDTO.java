package com.bookms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookUpdateDTO {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishDate;
    private Long categoryId;
    private String coverUrl;
    private String summary;
    private BigDecimal price;
    private String location;
    private Integer status;
}
