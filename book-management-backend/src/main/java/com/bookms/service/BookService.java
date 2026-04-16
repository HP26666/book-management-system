package com.bookms.service;

import com.bookms.dto.*;
import org.springframework.data.domain.Pageable;

public interface BookService {
    PageResponse<BookVO> listBooks(String keyword, Long categoryId, Integer status, Pageable pageable);
    BookVO getBookById(Long id);
    BookVO createBook(BookCreateDTO dto);
    BookVO updateBook(Long id, BookUpdateDTO dto);
    void deleteBook(Long id);
    void updateBookStatus(Long id, Integer status);
    void adjustStock(Long id, StockAdjustDTO dto);
}
