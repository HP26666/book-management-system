package com.bookms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookCreateDTO {
    private String isbn;
    @NotBlank(message = "书名不能为空")
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishDate;
    private Long categoryId;
    private String coverUrl;
    private String summary;
    private BigDecimal price;
    private Integer totalStock = 0;
    private String location;
}
