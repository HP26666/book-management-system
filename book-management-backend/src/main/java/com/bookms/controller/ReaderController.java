package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.ReaderSaveRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.ReaderResponse;
import com.bookms.service.ReaderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @GetMapping("/me")
    public ApiResponse<ReaderResponse> me() {
        return ApiResponse.success(readerService.currentReader());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<PageResponse<ReaderResponse>> pageReaders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String readerType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(readerService.pageReaders(keyword, readerType, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<ReaderResponse> getReader(@PathVariable Long id) {
        return ApiResponse.success(readerService.getReader(id));
    }

    @OperationLog("创建读者")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<ReaderResponse> create(@Valid @RequestBody ReaderSaveRequest request) {
        return ApiResponse.success("创建成功", readerService.create(request));
    }

    @OperationLog("更新读者")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<ReaderResponse> update(@PathVariable Long id, @Valid @RequestBody ReaderSaveRequest request) {
        return ApiResponse.success("更新成功", readerService.update(id, request));
    }

    @OperationLog("办理借阅证")
    @PutMapping("/{id}/card")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<ReaderResponse> issueCard(@PathVariable Long id) {
        return ApiResponse.success("办理成功", readerService.issueCard(id));
    }
}