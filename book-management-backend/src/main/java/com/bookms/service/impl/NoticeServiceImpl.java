package com.bookms.service.impl;

import com.bookms.dto.NoticeDTO;
import com.bookms.dto.PageResponse;
import com.bookms.entity.SysNotice;
import com.bookms.exception.BusinessException;
import com.bookms.repository.SysNoticeRepository;
import com.bookms.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeRepository noticeRepository;

    @Override
    public PageResponse<SysNotice> listNotices(String keyword, Pageable pageable) {
        Page<SysNotice> page = noticeRepository.findByConditions(keyword, pageable);
        return PageResponse.of(page);
    }

    @Override
    public List<SysNotice> getLatestNotices(int count) {
        return noticeRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(
                1, PageRequest.of(0, count));
    }

    @Override
    @Transactional
    public SysNotice createNotice(NoticeDTO dto) {
        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return noticeRepository.save(notice);
    }

    @Override
    @Transactional
    public SysNotice updateNotice(Long id, NoticeDTO dto) {
        SysNotice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        if (dto.getTitle() != null) notice.setTitle(dto.getTitle());
        if (dto.getContent() != null) notice.setContent(dto.getContent());
        if (dto.getType() != null) notice.setType(dto.getType());
        if (dto.getStatus() != null) notice.setStatus(dto.getStatus());
        return noticeRepository.save(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(Long id) {
        SysNotice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        notice.setDeleted(true);
        noticeRepository.save(notice);
    }
}
