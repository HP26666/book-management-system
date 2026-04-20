package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.BookSaveRequest;
import com.bookms.dto.request.StockAdjustRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.BookResponse;
import com.bookms.dto.response.IsbnMetadataResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.service.BookService;
import jakarta.validation.Valid;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ApiResponse<PageResponse<BookResponse>> pageBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ApiResponse.success(bookService.pageBooks(keyword, categoryId, status, page, size, !isManager(authentication)));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBook(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success(bookService.getBook(id, !isManager(authentication)));
    }

    @GetMapping("/isbn/{isbn}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<IsbnMetadataResponse> parseIsbn(@PathVariable String isbn) {
        return ApiResponse.success(bookService.parseIsbn(isbn));
    }

    @OperationLog("创建图书")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<BookResponse> create(@Valid @RequestBody BookSaveRequest request) {
        return ApiResponse.success("创建成功", bookService.create(request));
    }

    @OperationLog("更新图书")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookSaveRequest request) {
        return ApiResponse.success("更新成功", bookService.update(id, request));
    }

    @OperationLog("删除图书")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @OperationLog("调整库存")
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<BookResponse> adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustRequest request) {
        return ApiResponse.success("调整成功", bookService.adjustStock(id, request));
    }

    private boolean isManager(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()) || "ROLE_LIBRARIAN".equals(authority.getAuthority()));
    }
}