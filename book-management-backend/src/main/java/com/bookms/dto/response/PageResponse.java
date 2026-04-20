package com.bookms.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private final long total;

    private final int pages;

    private final int current;

    private final int size;

    private final List<T> records;

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber() + 1,
                page.getSize(),
                page.getContent());
    }
}