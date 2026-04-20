package com.bookms.service;

import com.bookms.dto.request.ReservationCreateRequest;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.ReservationResponse;

public interface ReservationService {

    ReservationResponse create(ReservationCreateRequest request);

    PageResponse<ReservationResponse> pageReservations(Integer status, Long userId, int page, int size);

    ReservationResponse cancel(Long id);

    void notifyNextReservation(Long bookId);

    void expireReservations();
}