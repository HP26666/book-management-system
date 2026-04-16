package com.bookms.controller;

import com.bookms.dto.ApiResponse;
import com.bookms.dto.DashboardVO;
import com.bookms.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('admin', 'librarian')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "仪表盘数据")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardVO> getDashboard() {
        return ApiResponse.ok(statisticsService.getDashboard());
    }
}
