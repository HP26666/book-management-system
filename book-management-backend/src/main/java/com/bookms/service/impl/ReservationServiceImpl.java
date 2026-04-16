package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.ReservationRecord;
import com.bookms.enums.ReservationStatus;
import com.bookms.exception.BusinessException;
import com.bookms.repository.*;
import com.bookms.security.SecurityUtils;
import com.bookms.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRecordRepository reservationRepository;
    private final BookInfoRepository bookInfoRepository;
    private final SysUserRepository userRepository;

    @Value("${bookms.reservation.expire-days:3}")
    private int expireDays;

    @Override
    public PageResponse<ReservationVO> listReservations(String keyword, Integer status, Pageable pageable) {
        Page<ReservationRecord> page = reservationRepository.findByConditions(keyword, status, pageable);
        return PageResponse.of(page.map(this::toReservationVO));
    }

    @Override
    public PageResponse<ReservationVO> listMyReservations(Integer status, Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<ReservationRecord> page;
        if (status != null) {
            page = reservationRepository.findByUserIdAndStatusAndDeletedFalse(userId, status, pageable);
        } else {
            page = reservationRepository.findByUserIdAndDeletedFalse(userId, pageable);
        }
        return PageResponse.of(page.map(this::toReservationVO));
    }

    @Override
    @Transactional
    public ReservationVO createReservation(Long bookId) {
        Long userId = SecurityUtils.getCurrentUserId();

        bookInfoRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException("图书不存在"));

        // Check duplicate reservation
        boolean exists = reservationRepository.existsActiveReservation(userId, bookId);
        if (exists) {
            throw new BusinessException("您已预约该图书");
        }

        ReservationRecord record = new ReservationRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setStatus(ReservationStatus.WAITING.getCode());
        record.setReserveDate(LocalDate.now());
        record.setExpireDate(LocalDate.now().plusDays(expireDays));
        reservationRepository.save(record);

        log.info("图书预约: 用户={}, 图书ID={}", userId, bookId);
        return toReservationVO(record);
    }

    @Override
    @Transactional
    public void cancelReservation(Long id) {
        ReservationRecord record = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));

        Long userId = SecurityUtils.getCurrentUserId();
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作他人的预约");
        }

        if (record.getStatus() != ReservationStatus.WAITING.getCode()) {
            throw new BusinessException("当前状态不允许取消");
        }

        record.setStatus(ReservationStatus.CANCELLED.getCode());
        reservationRepository.save(record);
    }

    @Override
    @Transactional
    public void processExpiredReservations() {
        List<ReservationRecord> expired = reservationRepository
                .findByStatusAndExpireDateBeforeAndDeletedFalse(ReservationStatus.WAITING.getCode(), LocalDate.now());
        for (ReservationRecord record : expired) {
            record.setStatus(ReservationStatus.EXPIRED.getCode());
            reservationRepository.save(record);
        }
        if (!expired.isEmpty()) {
            log.info("处理过期预约 {} 条", expired.size());
        }
    }

    private ReservationVO toReservationVO(ReservationRecord record) {
        ReservationVO.ReservationVOBuilder builder = ReservationVO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .bookId(record.getBookId())
                .status(record.getStatus())
                .statusDesc(ReservationStatus.fromCode(record.getStatus()).getDesc())
                .reserveDate(record.getReserveDate())
                .expireDate(record.getExpireDate())
                .notifyAt(record.getNotifyAt())
                .createdAt(record.getCreatedAt());

        userRepository.findById(record.getUserId()).ifPresent(u -> {
            builder.username(u.getUsername());
            builder.realName(u.getRealName());
        });

        bookInfoRepository.findById(record.getBookId()).ifPresent(b -> {
            builder.bookTitle(b.getTitle());
            builder.bookIsbn(b.getIsbn());
        });

        return builder.build();
    }
}
