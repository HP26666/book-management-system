package com.bookms.service;

import com.bookms.dto.*;
import org.springframework.data.domain.Pageable;

public interface BorrowService {
    PageResponse<BorrowVO> listBorrows(String keyword, Integer status, Pageable pageable);
    PageResponse<BorrowVO> listMyBorrows(Integer status, Pageable pageable);
    BorrowVO applyBorrow(BorrowApplyDTO dto);
    void approveBorrow(Long id);
    void rejectBorrow(Long id, String reason);
    void returnBook(Long id);
    void confirmReturn(Long id);
    void renewBorrow(Long id);
    long countOverdue();
    long countPendingApprovals();
}
