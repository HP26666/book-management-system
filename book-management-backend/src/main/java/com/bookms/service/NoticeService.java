package com.bookms.service;

import com.bookms.dto.NoticeDTO;
import com.bookms.dto.PageResponse;
import com.bookms.entity.SysNotice;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoticeService {
    PageResponse<SysNotice> listNotices(String keyword, Pageable pageable);
    List<SysNotice> getLatestNotices(int count);
    SysNotice createNotice(NoticeDTO dto);
    SysNotice updateNotice(Long id, NoticeDTO dto);
    void deleteNotice(Long id);
}
