package com.bookms.service;

import com.bookms.dto.request.BookSaveRequest;
import com.bookms.dto.request.StockAdjustRequest;
import com.bookms.dto.response.BookResponse;
import com.bookms.dto.response.IsbnMetadataResponse;
import com.bookms.dto.response.PageResponse;

public interface BookService {

    PageResponse<BookResponse> pageBooks(String keyword, Long categoryId, Integer status, int page, int size, boolean publicView);

    BookResponse getBook(Long id, boolean publicView);

    BookResponse create(BookSaveRequest request);

    BookResponse update(Long id, BookSaveRequest request);

    void delete(Long id);

    IsbnMetadataResponse parseIsbn(String isbn);

    BookResponse adjustStock(Long id, StockAdjustRequest request);
}