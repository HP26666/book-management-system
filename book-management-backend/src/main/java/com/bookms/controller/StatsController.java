package com.bookms.controller;

import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.DashboardStatsResponse;
import com.bookms.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public ApiResponse<DashboardStatsResponse> dashboard() {
        return ApiResponse.success(statsService.dashboard());
    }
}