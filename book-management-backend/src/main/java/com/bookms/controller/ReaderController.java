package com.bookms.controller;

import com.bookms.dto.*;
import com.bookms.service.ReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "读者管理")
@RestController
@RequestMapping("/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @Operation(summary = "读者列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    public ApiResponse<PageResponse<ReaderVO>> listReaders(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(readerService.listReaders(keyword, PageRequest.of(page - 1, size)));
    }

    @Operation(summary = "读者详情")
    @GetMapping("/user/{userId}")
    public ApiResponse<ReaderVO> getReaderByUserId(@PathVariable Long userId) {
        return ApiResponse.ok(readerService.getReaderByUserId(userId));
    }

    @Operation(summary = "更新读者信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    public ApiResponse<ReaderVO> updateReader(@PathVariable Long id, @Valid @RequestBody ReaderDTO dto) {
        return ApiResponse.ok(readerService.updateReader(id, dto));
    }

    @Operation(summary = "黑名单开关")
    @PatchMapping("/{id}/blacklist")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    public ApiResponse<Void> toggleBlacklist(@PathVariable Long id, @RequestParam Boolean blacklist) {
        readerService.toggleBlacklist(id, blacklist);
        return ApiResponse.ok(null);
    }
}
