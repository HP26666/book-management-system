package com.bookms.controller;

import com.bookms.aspect.OperationLog;
import com.bookms.dto.ApiResponse;
import com.bookms.dto.NoticeDTO;
import com.bookms.dto.PageResponse;
import com.bookms.entity.SysNotice;
import com.bookms.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公告管理")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "公告列表")
    @GetMapping
    public ApiResponse<PageResponse<SysNotice>> listNotices(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(noticeService.listNotices(keyword,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Operation(summary = "最新公告")
    @GetMapping("/latest")
    public ApiResponse<List<SysNotice>> getLatestNotices(
            @RequestParam(defaultValue = "5") int count) {
        return ApiResponse.ok(noticeService.getLatestNotices(count));
    }

    @Operation(summary = "创建公告")
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("创建公告")
    public ApiResponse<SysNotice> createNotice(@Valid @RequestBody NoticeDTO dto) {
        return ApiResponse.ok(noticeService.createNotice(dto));
    }

    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("更新公告")
    public ApiResponse<SysNotice> updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeDTO dto) {
        return ApiResponse.ok(noticeService.updateNotice(id, dto));
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("删除公告")
    public ApiResponse<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ApiResponse.ok(null);
    }
}
