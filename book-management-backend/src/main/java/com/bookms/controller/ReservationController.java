package com.bookms.controller;

import com.bookms.dto.*;
import com.bookms.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "预约管理")
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "预约列表(管理)")
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'librarian')")
    public ApiResponse<PageResponse<ReservationVO>> listReservations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(reservationService.listReservations(keyword, status,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Operation(summary = "我的预约")
    @GetMapping("/my")
    public ApiResponse<PageResponse<ReservationVO>> listMyReservations(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(reservationService.listMyReservations(status,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Operation(summary = "预约图书")
    @PostMapping
    public ApiResponse<ReservationVO> createReservation(@RequestParam Long bookId) {
        return ApiResponse.ok(reservationService.createReservation(bookId));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ApiResponse.ok(null);
    }
}
