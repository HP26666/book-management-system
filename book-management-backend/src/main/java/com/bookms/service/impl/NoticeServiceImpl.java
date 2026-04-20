package com.bookms.service.impl;

import com.bookms.dto.request.NoticeSaveRequest;
import com.bookms.dto.response.NoticeResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.entity.Notice;
import com.bookms.entity.User;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.NoticeMapper;
import com.bookms.repository.NoticeRepository;
import com.bookms.repository.UserRepository;
import com.bookms.security.SecurityUtils;
import com.bookms.service.NoticeService;
import com.bookms.util.PageableUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final NoticeMapper noticeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoticeResponse> pageNotices(Short type, Short status, int page, int size, boolean manageView) {
        Pageable pageable = PageableUtils.of(page, size, Sort.by(Sort.Direction.DESC, "publishTime"));
        Page<NoticeResponse> result = noticeRepository.findAll(buildSpecification(type, status, manageView), pageable)
                .map(noticeMapper::toResponse);
        return PageResponse.of(result);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long id, boolean manageView) {
        Notice notice = noticeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "公告不存在"));
        if (!manageView && notice.getStatus() != 1) {
            throw new BusinessException(404, "公告不存在");
        }
        return noticeMapper.toResponse(notice);
    }

    @Override
    @Transactional
    public NoticeResponse create(NoticeSaveRequest request) {
        Notice notice = noticeMapper.toEntity(request);
        notice.setPublisher(currentOperator());
        if (notice.getPublishTime() == null && notice.getStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        return noticeMapper.toResponse(noticeRepository.save(notice));
    }

    @Override
    @Transactional
    public NoticeResponse update(Long id, NoticeSaveRequest request) {
        Notice notice = noticeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "公告不存在"));
        noticeMapper.updateEntity(request, notice);
        if (notice.getPublishTime() == null && notice.getStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        return noticeMapper.toResponse(noticeRepository.save(notice));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Notice notice = noticeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "公告不存在"));
        noticeRepository.delete(notice);
    }

    private Specification<Notice> buildSpecification(Short type, Short status, boolean manageView) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (manageView) {
                if (status != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
            } else {
                predicates.add(criteriaBuilder.equal(root.get("status"), (short) 1));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private User currentOperator() {
        return userRepository.findByIdAndDeletedFalse(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(404, "当前用户不存在"));
    }
}