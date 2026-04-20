package com.bookms.controller;

import com.bookms.annotation.OperationLog;
import com.bookms.dto.request.ReservationCreateRequest;
import com.bookms.dto.response.ApiResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.ReservationResponse;
import com.bookms.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @OperationLog("提交预约申请")
    @PostMapping
    public ApiResponse<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request) {
        return ApiResponse.success("预约成功", reservationService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<ReservationResponse>> pageReservations(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(reservationService.pageReservations(status, userId, page, size));
    }

    @OperationLog("取消预约")
    @PutMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success("取消成功", reservationService.cancel(id));
    }
}