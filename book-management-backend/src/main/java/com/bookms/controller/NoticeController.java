package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.NoticeSaveRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.NoticeResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.service.NoticeService;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ApiResponse<PageResponse<NoticeResponse>> pageNotices(
            @RequestParam(required = false) Short type,
            @RequestParam(required = false) Short status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ApiResponse.success(noticeService.pageNotices(type, status, page, size, isManager(authentication)));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticeResponse> getNotice(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success(noticeService.getNotice(id, isManager(authentication)));
    }

    @OperationLog("发布公告")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticeResponse> create(@Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.success("发布成功", noticeService.create(request));
    }

    @OperationLog("更新公告")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticeResponse> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.success("更新成功", noticeService.update(id, request));
    }

    @OperationLog("删除公告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    private boolean isManager(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()) || "ROLE_LIBRARIAN".equals(authority.getAuthority()));
    }
}