package com.bookms.service;

import com.bookms.dto.*;
import org.springframework.data.domain.Pageable;

public interface ReaderService {
    PageResponse<ReaderVO> listReaders(String keyword, Pageable pageable);
    ReaderVO getReaderByUserId(Long userId);
    ReaderVO updateReader(Long id, ReaderDTO dto);
    void toggleBlacklist(Long id, Boolean blacklist);
}
