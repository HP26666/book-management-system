package com.bookms.service.impl;

import com.bookms.config.BookmsProperties;
import com.bookms.dto.request.BorrowApplyRequest;
import com.bookms.dto.request.BorrowRejectRequest;
import com.bookms.dto.response.BorrowResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.entity.Book;
import com.bookms.entity.BorrowRecord;
import com.bookms.entity.Reader;
import com.bookms.entity.StockLog;
import com.bookms.entity.User;
import com.bookms.enums.BookStatus;
import com.bookms.enums.BorrowStatus;
import com.bookms.enums.StockChangeType;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.BorrowMapper;
import com.bookms.repository.BookRepository;
import com.bookms.repository.BorrowRecordRepository;
import com.bookms.repository.ReaderRepository;
import com.bookms.repository.StockLogRepository;
import com.bookms.repository.UserRepository;
import com.bookms.security.SecurityUtils;
import com.bookms.service.BorrowService;
import com.bookms.service.ReservationService;
import com.bookms.util.BorrowNoGenerator;
import com.bookms.util.PageableUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final UserRepository userRepository;
    private final StockLogRepository stockLogRepository;
    private final BorrowMapper borrowMapper;
    private final BorrowNoGenerator borrowNoGenerator;
    private final ReservationService reservationService;
    private final BookmsProperties bookmsProperties;

    @Override
    @Transactional
    public BorrowResponse apply(BorrowApplyRequest request) {
        refreshOverdueStatuses();
        Long userId = SecurityUtils.getCurrentUserId();
        Reader reader = readerRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(400, "请先完善读者信息"));
        validateReaderEligible(reader);

        Book book = bookRepository.findByIdAndDeletedFalse(request.getBookId())
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));
        if (book.getStatus() != BookStatus.ONLINE) {
            throw new BusinessException(409, "图书已下架");
        }
        if (book.getAvailableStock() <= 0) {
            throw new BusinessException(409, "当前图书无可借库存");
        }

        long activeBorrowCount = borrowRecordRepository.countByUserIdAndStatusIn(
                userId,
                List.of(
                        BorrowStatus.APPLYING,
                        BorrowStatus.APPROVED_PENDING_PICKUP,
                        BorrowStatus.BORROWING,
                        BorrowStatus.OVERDUE));
        if (activeBorrowCount >= reader.getMaxBorrowCount()) {
            throw new BusinessException(409, "已超过借阅上限");
        }

        BorrowRecord record = new BorrowRecord();
        record.setUser(reader.getUser());
        record.setBook(book);
        record.setBorrowNo(borrowNoGenerator.next());
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(request.getBorrowDays()));
        record.setStatus(BorrowStatus.APPLYING);
        record.setFineAmount(BigDecimal.ZERO);
        return borrowMapper.toResponse(borrowRecordRepository.save(record));
    }

    @Override
    @Transactional
    public PageResponse<BorrowResponse> pageBorrows(Integer status, Long userId, String keyword, int page, int size) {
        refreshOverdueStatuses();
        boolean manager = SecurityUtils.hasAnyRole("ADMIN", "LIBRARIAN");
        Long resolvedUserId = manager ? userId : SecurityUtils.getCurrentUserId();
        Pageable pageable = PageableUtils.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BorrowResponse> result = borrowRecordRepository.findAll(buildSpecification(status, resolvedUserId, keyword), pageable)
                .map(borrowMapper::toResponse);
        return PageResponse.of(result);
    }

    @Override
    @Transactional
    public BorrowResponse getBorrow(Long id) {
        refreshOverdueStatuses();
        BorrowRecord record = getBorrowEntity(id);
        if (!SecurityUtils.hasAnyRole("ADMIN", "LIBRARIAN") && !record.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权查看该借阅记录");
        }
        return borrowMapper.toResponse(record);
    }

    @Override
    @Transactional
    public BorrowResponse approve(Long id) {
        BorrowRecord record = getBorrowEntity(id);
        if (record.getStatus() != BorrowStatus.APPLYING) {
            throw new BusinessException(409, "当前状态不能审批通过");
        }
        int affectedRows = bookRepository.decreaseAvailableStock(record.getBook().getId());
        if (affectedRows == 0) {
            throw new BusinessException(409, "库存不足");
        }

        User operator = getCurrentOperator();
        record.setStatus(BorrowStatus.APPROVED_PENDING_PICKUP);
        record.setApproveUser(operator);
        record.setApproveTime(LocalDateTime.now());
        BorrowRecord saved = borrowRecordRepository.save(record);

        Reader reader = readerRepository.findByUserIdAndDeletedFalse(saved.getUser().getId())
                .orElseThrow(() -> new BusinessException(404, "读者信息不存在"));
        reader.setCurrentBorrowCount(reader.getCurrentBorrowCount() + 1);
        readerRepository.save(reader);

        saveStockLog(saved.getBook(), StockChangeType.BORROW, -1, saved.getBook().getAvailableStock(), saved.getBook().getAvailableStock() - 1, saved.getId(), "审批通过扣减库存");
        return borrowMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BorrowResponse reject(Long id, BorrowRejectRequest request) {
        BorrowRecord record = getBorrowEntity(id);
        if (record.getStatus() != BorrowStatus.APPLYING) {
            throw new BusinessException(409, "当前状态不能拒绝");
        }
        record.setStatus(BorrowStatus.REJECTED);
        record.setRejectReason(request.getRejectReason());
        record.setApproveUser(getCurrentOperator());
        record.setApproveTime(LocalDateTime.now());
        return borrowMapper.toResponse(borrowRecordRepository.save(record));
    }

    @Override
    @Transactional
    public BorrowResponse returnBorrow(Long id) {
        refreshOverdueStatuses();
        BorrowRecord record = getBorrowEntity(id);
        if (!(record.getStatus() == BorrowStatus.APPROVED_PENDING_PICKUP
                || record.getStatus() == BorrowStatus.BORROWING
                || record.getStatus() == BorrowStatus.OVERDUE)) {
            throw new BusinessException(409, "当前状态不能归还");
        }

        int affectedRows = bookRepository.increaseAvailableStock(record.getBook().getId());
        if (affectedRows == 0) {
            throw new BusinessException(409, "库存恢复失败");
        }

        record.setReturnDate(LocalDate.now());
        record.setFineAmount(calculateFine(record));
        record.setStatus(BorrowStatus.RETURNED);
        BorrowRecord saved = borrowRecordRepository.save(record);

        readerRepository.findByUserIdAndDeletedFalse(saved.getUser().getId()).ifPresent(reader -> {
            reader.setCurrentBorrowCount(Math.max(0, reader.getCurrentBorrowCount() - 1));
            readerRepository.save(reader);
        });

        saveStockLog(saved.getBook(), StockChangeType.RETURN, 1, saved.getBook().getAvailableStock(), saved.getBook().getAvailableStock() + 1, saved.getId(), "归还图书恢复库存");
        reservationService.notifyNextReservation(saved.getBook().getId());
        return borrowMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BorrowResponse renew(Long id) {
        refreshOverdueStatuses();
        BorrowRecord record = getBorrowEntity(id);
        boolean manager = SecurityUtils.hasAnyRole("ADMIN", "LIBRARIAN");
        if (!manager && !record.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权续借他人的借阅记录");
        }
        if (!(record.getStatus() == BorrowStatus.APPROVED_PENDING_PICKUP || record.getStatus() == BorrowStatus.BORROWING)) {
            throw new BusinessException(409, "当前状态不能续借");
        }
        if (record.getRenewCount() >= bookmsProperties.getBorrow().getRenewMaxCount()) {
            throw new BusinessException(409, "已达到最大续借次数");
        }
        if (record.getDueDate() != null && record.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessException(409, "逾期图书不可续借");
        }
        record.setRenewCount(record.getRenewCount() + 1);
        record.setDueDate(record.getDueDate().plusDays(bookmsProperties.getBorrow().getRenewDays()));
        return borrowMapper.toResponse(borrowRecordRepository.save(record));
    }

    @Override
    @Transactional
    public void refreshOverdueStatuses() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(
                LocalDate.now(),
                List.of(BorrowStatus.APPROVED_PENDING_PICKUP, BorrowStatus.BORROWING));
        if (overdueRecords.isEmpty()) {
            return;
        }
        overdueRecords.forEach(record -> record.setStatus(BorrowStatus.OVERDUE));
        borrowRecordRepository.saveAll(overdueRecords);
    }

    private BorrowRecord getBorrowEntity(Long id) {
        return borrowRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "借阅记录不存在"));
    }

    private User getCurrentOperator() {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return userRepository.findByIdAndDeletedFalse(operatorId)
                .orElseThrow(() -> new BusinessException(404, "当前用户不存在"));
    }

    private void validateReaderEligible(Reader reader) {
        if (!StringUtils.hasText(reader.getReaderCardNo())) {
            throw new BusinessException(409, "请先办理借阅证");
        }
        if (Boolean.TRUE.equals(reader.getBlacklist())) {
            throw new BusinessException(409, "读者已被拉黑，不能借阅");
        }
        LocalDate today = LocalDate.now();
        if (reader.getValidDateStart() != null && today.isBefore(reader.getValidDateStart())) {
            throw new BusinessException(409, "借阅证尚未生效");
        }
        if (reader.getValidDateEnd() != null && today.isAfter(reader.getValidDateEnd())) {
            throw new BusinessException(409, "借阅证已过期");
        }
    }

    private BigDecimal calculateFine(BorrowRecord record) {
        if (record.getDueDate() == null || record.getReturnDate() == null || !record.getReturnDate().isAfter(record.getDueDate())) {
            return BigDecimal.ZERO;
        }
        long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), record.getReturnDate());
        BigDecimal fine = bookmsProperties.getBorrow().getDailyFineRate().multiply(BigDecimal.valueOf(overdueDays));
        BigDecimal bookPrice = record.getBook().getPrice() == null ? BigDecimal.ZERO : record.getBook().getPrice();
        BigDecimal maxFine = bookPrice.multiply(bookmsProperties.getBorrow().getMaxFinePriceRatio());
        return fine.min(maxFine).setScale(2, RoundingMode.HALF_UP);
    }

    private void saveStockLog(Book book, StockChangeType type, int changeQty, int beforeQty, int afterQty, Long borrowId, String remark) {
        StockLog log = new StockLog();
        log.setBook(book);
        log.setChangeType(type.name().toLowerCase());
        log.setChangeQty(changeQty);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(afterQty);
        log.setOperator(getCurrentOperator());
        log.setRelatedBorrowId(borrowId);
        log.setRemark(remark);
        stockLogRepository.save(log);
    }

    private Specification<BorrowRecord> buildSpecification(Integer status, Long userId, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var userJoin = root.join("user", JoinType.LEFT);
            var bookJoin = root.join("book", JoinType.LEFT);
            if (status != null) {
                BorrowStatus[] values = BorrowStatus.values();
                if (status < 0 || status >= values.length) {
                    throw new BusinessException(400, "无效的借阅状态: " + status);
                }
                predicates.add(criteriaBuilder.equal(root.get("status"), values[status]));
            }
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("borrowNo")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(bookJoin.get("title")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("username")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("realName")), like)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}