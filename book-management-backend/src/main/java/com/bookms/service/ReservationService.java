package com.bookms.service;

import com.bookms.dto.*;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    PageResponse<ReservationVO> listReservations(String keyword, Integer status, Pageable pageable);
    PageResponse<ReservationVO> listMyReservations(Integer status, Pageable pageable);
    ReservationVO createReservation(Long bookId);
    void cancelReservation(Long id);
    void processExpiredReservations();
}
