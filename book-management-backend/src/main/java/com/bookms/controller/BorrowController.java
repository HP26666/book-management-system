package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.BorrowApplyRequest;
import com.bookms.dto.request.BorrowRejectRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.BorrowResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.service.BorrowService;
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
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @OperationLog("提交借阅申请")
    @PostMapping
    public ApiResponse<BorrowResponse> apply(@Valid @RequestBody BorrowApplyRequest request) {
        return ApiResponse.success("借阅申请已提交", borrowService.apply(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<BorrowResponse>> pageBorrows(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(borrowService.pageBorrows(status, userId, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<BorrowResponse> getBorrow(@PathVariable Long id) {
        return ApiResponse.success(borrowService.getBorrow(id));
    }

    @OperationLog("审批借阅")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<BorrowResponse> approve(@PathVariable Long id) {
        return ApiResponse.success("审批通过", borrowService.approve(id));
    }

    @OperationLog("拒绝借阅")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<BorrowResponse> reject(@PathVariable Long id, @Valid @RequestBody BorrowRejectRequest request) {
        return ApiResponse.success("已拒绝", borrowService.reject(id, request));
    }

    @OperationLog("归还图书")
    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<BorrowResponse> returnBorrow(@PathVariable Long id) {
        return ApiResponse.success("归还成功", borrowService.returnBorrow(id));
    }

    @OperationLog("续借图书")
    @PutMapping("/{id}/renew")
    public ApiResponse<BorrowResponse> renew(@PathVariable Long id) {
        return ApiResponse.success("续借成功", borrowService.renew(id));
    }
}