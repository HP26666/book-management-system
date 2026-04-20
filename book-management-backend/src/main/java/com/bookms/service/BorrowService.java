package com.bookms.service;

import com.bookms.dto.request.BorrowApplyRequest;
import com.bookms.dto.request.BorrowRejectRequest;
import com.bookms.dto.response.BorrowResponse;
import com.bookms.dto.response.PageResponse;

public interface BorrowService {

    BorrowResponse apply(BorrowApplyRequest request);

    PageResponse<BorrowResponse> pageBorrows(Integer status, Long userId, String keyword, int page, int size);

    BorrowResponse getBorrow(Long id);

    BorrowResponse approve(Long id);

    BorrowResponse reject(Long id, BorrowRejectRequest request);

    BorrowResponse returnBorrow(Long id);

    BorrowResponse renew(Long id);

    void refreshOverdueStatuses();
}