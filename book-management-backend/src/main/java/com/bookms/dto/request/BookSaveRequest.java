package com.bookms.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookSaveRequest {

    @Size(max = 20, message = "isbn 长度不能超过 20")
    private String isbn;

    @NotBlank(message = "title 不能为空")
    @Size(max = 255, message = "title 长度不能超过 255")
    private String title;

    @Size(max = 255, message = "author 长度不能超过 255")
    private String author;

    @Size(max = 100, message = "publisher 长度不能超过 100")
    private String publisher;

    private LocalDate publishDate;

    @NotNull(message = "categoryId 不能为空")
    private Long categoryId;

    private String coverUrl;

    private String summary;

    @DecimalMin(value = "0", message = "price 不能小于 0")
    private BigDecimal price;

    @NotNull(message = "totalStock 不能为空")
    @PositiveOrZero(message = "totalStock 不能小于 0")
    private Integer totalStock;

    @PositiveOrZero(message = "availableStock 不能小于 0")
    private Integer availableStock;

    private Integer status;

    @Size(max = 100, message = "location 长度不能超过 100")
    private String location;
}