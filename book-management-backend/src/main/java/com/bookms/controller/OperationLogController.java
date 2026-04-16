package com.bookms.controller;

import com.bookms.dto.ApiResponse;
import com.bookms.dto.PageResponse;
import com.bookms.entity.SysOperationLog;
import com.bookms.repository.SysOperationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
public class OperationLogController {

    private final SysOperationLogRepository operationLogRepository;

    @Operation(summary = "操作日志列表")
    @GetMapping
    public ApiResponse<PageResponse<SysOperationLog>> listLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SysOperationLog> pageResult = operationLogRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok(PageResponse.of(pageResult));
    }
}
