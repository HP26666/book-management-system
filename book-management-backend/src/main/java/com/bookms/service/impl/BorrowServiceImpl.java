package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.BookInfo;
import com.bookms.entity.BorrowRecord;
import com.bookms.entity.ReaderInfo;
import com.bookms.entity.ReservationRecord;
import com.bookms.entity.SysUser;
import com.bookms.enums.BorrowStatus;
import com.bookms.enums.ReservationStatus;
import com.bookms.exception.BusinessException;
import com.bookms.repository.*;
import com.bookms.security.SecurityUtils;
import com.bookms.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookInfoRepository bookInfoRepository;
    private final SysUserRepository userRepository;
    private final ReaderInfoRepository readerInfoRepository;
    private final ReservationRecordRepository reservationRecordRepository;

    @Value("${bookms.borrow.default-days:30}")
    private int defaultBorrowDays;

    @Value("${bookms.borrow.renew-days:15}")
    private int renewDays;

    @Value("${bookms.borrow.max-renew-count:1}")
    private int maxRenewCount;

    @Value("${bookms.borrow.daily-fine-rate:0.5}")
    private double dailyFineRate;

    @Value("${bookms.borrow.max-fine:50.0}")
    private double maxFine;

    @Override
    public PageResponse<BorrowVO> listBorrows(String keyword, Integer status, Pageable pageable) {
        Page<BorrowRecord> page = borrowRecordRepository.findByConditions(keyword, status, pageable);
        return PageResponse.of(page.map(this::toBorrowVO));
    }

    @Override
    public PageResponse<BorrowVO> listMyBorrows(Integer status, Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<BorrowRecord> page;
        if (status != null) {
            page = borrowRecordRepository.findByUserIdAndStatusAndDeletedFalse(userId, status, pageable);
        } else {
            page = borrowRecordRepository.findByUserIdAndDeletedFalse(userId, pageable);
        }
        return PageResponse.of(page.map(this::toBorrowVO));
    }

    @Override
    @Transactional
    public BorrowVO applyBorrow(BorrowApplyDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();

        // Check reader info
        ReaderInfo reader = readerInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("读者信息不存在，请先完善读者信息"));

        if (reader.getIsBlacklist()) {
            throw new BusinessException("您已被加入黑名单，无法借阅");
        }

        if (reader.getCurrentBorrowCount() >= reader.getMaxBorrowCount()) {
            throw new BusinessException("您的借阅数量已达上限(" + reader.getMaxBorrowCount() + "本)");
        }

        BookInfo book = bookInfoRepository.findById(dto.getBookId())
                .orElseThrow(() -> new BusinessException("图书不存在"));

        if (book.getAvailableStock() <= 0) {
            throw new BusinessException("该图书暂无库存，请预约");
        }

        // Check duplicate active borrow
        boolean hasBorrow = borrowRecordRepository.existsActiveBorrow(userId, dto.getBookId());
        if (hasBorrow) {
            throw new BusinessException("您已有该图书的借阅申请");
        }

        BorrowRecord record = new BorrowRecord();
        record.setBorrowNo("BR" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        record.setUserId(userId);
        record.setBookId(dto.getBookId());
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(defaultBorrowDays));
        record.setStatus(BorrowStatus.PENDING.getCode());
        record.setRenewCount(0);
        borrowRecordRepository.save(record);

        log.info("借阅申请: 用户={}, 图书={}", userId, book.getTitle());
        return toBorrowVO(record);
    }

    @Override
    @Transactional
    public void approveBorrow(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));

        if (record.getStatus() != BorrowStatus.PENDING.getCode()) {
            throw new BusinessException("当前状态不允许审批");
        }

        // Decrement stock atomically
        int updated = bookInfoRepository.decrementStock(record.getBookId());
        if (updated == 0) {
            throw new BusinessException("图书库存不足，无法审批");
        }

        record.setStatus(BorrowStatus.BORROWED.getCode());
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(defaultBorrowDays));
        record.setApproveUserId(SecurityUtils.getCurrentUserId());
        record.setApproveTime(LocalDateTime.now());
        borrowRecordRepository.save(record);

        // Update reader current borrow count
        readerInfoRepository.findByUserId(record.getUserId()).ifPresent(reader -> {
            reader.setCurrentBorrowCount(reader.getCurrentBorrowCount() + 1);
            readerInfoRepository.save(reader);
        });

        log.info("借阅审批通过: {}", record.getBorrowNo());
    }

    @Override
    @Transactional
    public void rejectBorrow(Long id, String reason) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));

        if (record.getStatus() != BorrowStatus.PENDING.getCode()) {
            throw new BusinessException("当前状态不允许驳回");
        }

        record.setStatus(BorrowStatus.REJECTED.getCode());
        record.setApproveUserId(SecurityUtils.getCurrentUserId());
        record.setApproveTime(LocalDateTime.now());
        record.setRejectReason(reason);
        borrowRecordRepository.save(record);

        log.info("借阅审批驳回: {}, 原因: {}", record.getBorrowNo(), reason);
    }

    @Override
    @Transactional
    public void returnBook(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));

        if (record.getStatus() != BorrowStatus.BORROWED.getCode() &&
            record.getStatus() != BorrowStatus.OVERDUE.getCode()) {
            throw new BusinessException("当前状态不允许还书");
        }

        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowStatus.RETURNED.getCode());

        // Calculate fine
        if (record.getDueDate() != null && LocalDate.now().isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
            BigDecimal fine = BigDecimal.valueOf(overdueDays * dailyFineRate);
            if (fine.doubleValue() > maxFine) {
                fine = BigDecimal.valueOf(maxFine);
            }
            record.setFineAmount(fine);
        }

        borrowRecordRepository.save(record);

        // Increment stock
        bookInfoRepository.incrementStock(record.getBookId());

        // Notify first waiting reservation
        List<ReservationRecord> queue = reservationRecordRepository.findQueueByBookId(record.getBookId());
        if (!queue.isEmpty()) {
            ReservationRecord first = queue.get(0);
            first.setStatus(ReservationStatus.NOTIFIED.getCode());
            first.setNotifyAt(LocalDateTime.now());
            reservationRecordRepository.save(first);
            log.info("预约通知: 用户={} 图书={}", first.getUserId(), record.getBookId());
        }

        // Update reader borrow count
        readerInfoRepository.findByUserId(record.getUserId()).ifPresent(reader -> {
            reader.setCurrentBorrowCount(Math.max(0, reader.getCurrentBorrowCount() - 1));
            readerInfoRepository.save(reader);
        });

        log.info("图书归还: {}", record.getBorrowNo());
    }

    @Override
    @Transactional
    public void confirmReturn(Long id) {
        // Admin confirms the return
        returnBook(id);
    }

    @Override
    @Transactional
    public void renewBorrow(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));

        if (record.getStatus() != BorrowStatus.BORROWED.getCode()) {
            throw new BusinessException("当前状态不允许续借");
        }

        if (record.getRenewCount() >= maxRenewCount) {
            throw new BusinessException("续借次数已达上限");
        }

        record.setDueDate(record.getDueDate().plusDays(renewDays));
        record.setRenewCount(record.getRenewCount() + 1);
        borrowRecordRepository.save(record);

        log.info("续借成功: {}, 新到期日: {}", record.getBorrowNo(), record.getDueDate());
    }

    @Override
    public long countOverdue() {
        return borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.OVERDUE.getCode());
    }

    @Override
    public long countPendingApprovals() {
        return borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.PENDING.getCode());
    }

    private BorrowVO toBorrowVO(BorrowRecord record) {
        BorrowVO.BorrowVOBuilder builder = BorrowVO.builder()
                .id(record.getId())
                .borrowNo(record.getBorrowNo())
                .userId(record.getUserId())
                .bookId(record.getBookId())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .statusDesc(BorrowStatus.fromCode(record.getStatus()).getDesc())
                .renewCount(record.getRenewCount())
                .fineAmount(record.getFineAmount())
                .approveUserId(record.getApproveUserId())
                .approveTime(record.getApproveTime())
                .rejectReason(record.getRejectReason())
                .remark(record.getRemark())
                .createdAt(record.getCreatedAt());

        // Populate user info
        userRepository.findById(record.getUserId()).ifPresent(u -> {
            builder.username(u.getUsername());
            builder.realName(u.getRealName());
        });

        // Populate book info
        bookInfoRepository.findById(record.getBookId()).ifPresent(b -> {
            builder.bookTitle(b.getTitle());
            builder.bookIsbn(b.getIsbn());
        });

        if (record.getApproveUserId() != null) {
            userRepository.findById(record.getApproveUserId())
                    .ifPresent(u -> builder.approveUsername(u.getUsername()));
        }

        return builder.build();
    }
}
