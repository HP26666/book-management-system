package com.bookms.controller;

import com.bookms.aspect.OperationLog;
import com.bookms.dto.*;
import com.bookms.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "借阅管理")
@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @Operation(summary = "借阅列表(管理)")
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    public ApiResponse<PageResponse<BorrowVO>> listBorrows(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(borrowService.listBorrows(keyword, status,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Operation(summary = "我的借阅")
    @GetMapping("/my")
    public ApiResponse<PageResponse<BorrowVO>> listMyBorrows(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(borrowService.listMyBorrows(status,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Operation(summary = "申请借阅")
    @PostMapping("/apply")
    @OperationLog("申请借阅")
    public ApiResponse<BorrowVO> applyBorrow(@Valid @RequestBody BorrowApplyDTO dto) {
        return ApiResponse.ok(borrowService.applyBorrow(dto));
    }

    @Operation(summary = "审批通过")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("借阅审批通过")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        borrowService.approveBorrow(id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("借阅审批驳回")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        borrowService.rejectBorrow(id, reason);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "归还图书")
    @PostMapping("/{id}/return")
    @OperationLog("归还图书")
    public ApiResponse<Void> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "确认归还")
    @PostMapping("/{id}/confirm-return")
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    @OperationLog("确认归还")
    public ApiResponse<Void> confirmReturn(@PathVariable Long id) {
        borrowService.confirmReturn(id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "续借")
    @PostMapping("/{id}/renew")
    @OperationLog("续借")
    public ApiResponse<Void> renew(@PathVariable Long id) {
        borrowService.renewBorrow(id);
        return ApiResponse.ok(null);
    }
}
