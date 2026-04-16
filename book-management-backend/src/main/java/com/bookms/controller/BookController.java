package com.bookms.controller;

import com.bookms.aspect.OperationLog;
import com.bookms.dto.*;
import com.bookms.service.BookService;
import com.bookms.service.IsbnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "图书管理")
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final IsbnService isbnService;

    @Operation(summary = "ISBN查询")
    @GetMapping("/isbn/{isbn}")
    public ApiResponse<BookVO> lookupIsbn(@PathVariable String isbn) {
        BookVO result = isbnService.lookupByIsbn(isbn);
        if (result == null) {
            return ApiResponse.ok(null);
        }
        return ApiResponse.ok(result);
    }

    @Operation(summary = "图书列表")
    @GetMapping
    public ApiResponse<PageResponse<BookVO>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(bookService.listBooks(keyword, categoryId, status,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Operation(summary = "图书详情")
    @GetMapping("/{id}")
    public ApiResponse<BookVO> getBookById(@PathVariable Long id) {
        return ApiResponse.ok(bookService.getBookById(id));
    }

    @Operation(summary = "新增图书")
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("新增图书")
    public ApiResponse<BookVO> createBook(@Valid @RequestBody BookCreateDTO dto) {
        return ApiResponse.ok(bookService.createBook(dto));
    }

    @Operation(summary = "更新图书")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("更新图书")
    public ApiResponse<BookVO> updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateDTO dto) {
        return ApiResponse.ok(bookService.updateBook(id, dto));
    }

    @Operation(summary = "删除图书")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("删除图书")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "上下架")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bookService.updateBookStatus(id, status);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "库存调整")
    @PostMapping("/{id}/stock-adjust")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("库存调整")
    public ApiResponse<Void> adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustDTO dto) {
        bookService.adjustStock(id, dto);
        return ApiResponse.ok(null);
    }
}
