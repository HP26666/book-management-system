package com.bookms.service.impl;

import com.bookms.config.BookmsProperties;
import com.bookms.dto.request.ReservationCreateRequest;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.ReservationResponse;
import com.bookms.entity.Book;
import com.bookms.entity.ReservationRecord;
import com.bookms.entity.User;
import com.bookms.enums.BookStatus;
import com.bookms.enums.ReservationStatus;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.ReservationMapper;
import com.bookms.repository.BookRepository;
import com.bookms.repository.ReservationRepository;
import com.bookms.repository.UserRepository;
import com.bookms.security.SecurityUtils;
import com.bookms.service.ReservationService;
import com.bookms.util.PageableUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
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
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final BookmsProperties bookmsProperties;

    @Override
    @Transactional
    public ReservationResponse create(ReservationCreateRequest request) {
        expireReservations();
        Long userId = SecurityUtils.getCurrentUserId();
        Book book = bookRepository.findByIdAndDeletedFalse(request.getBookId())
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));
        if (book.getStatus() != BookStatus.ONLINE) {
            throw new BusinessException(409, "图书已下架，无法预约");
        }
        if (book.getAvailableStock() > 0) {
            throw new BusinessException(409, "当前图书仍有库存，不能预约");
        }
        if (reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                userId,
                book.getId(),
                List.of(ReservationStatus.RESERVED, ReservationStatus.NOTIFIED))) {
            throw new BusinessException(409, "已存在有效预约记录");
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        ReservationRecord record = new ReservationRecord();
        record.setUser(user);
        record.setBook(book);
        record.setStatus(ReservationStatus.RESERVED);
        record.setReserveDate(LocalDate.now());
        record.setExpireDate(LocalDate.now().plusDays(bookmsProperties.getBorrow().getReservationExpireDays()));
        return reservationMapper.toResponse(reservationRepository.save(record));
    }

    @Override
    @Transactional
    public PageResponse<ReservationResponse> pageReservations(Integer status, Long userId, int page, int size) {
        expireReservations();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean manager = SecurityUtils.hasAnyRole("ADMIN", "LIBRARIAN");
        Long resolvedUserId = manager ? userId : currentUserId;
        Pageable pageable = PageableUtils.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReservationResponse> result = reservationRepository.findAll(buildSpecification(status, resolvedUserId), pageable)
                .map(reservationMapper::toResponse);
        return PageResponse.of(result);
    }

    @Override
    @Transactional
    public ReservationResponse cancel(Long id) {
        expireReservations();
        ReservationRecord record = reservationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "预约记录不存在"));
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean manager = SecurityUtils.hasAnyRole("ADMIN", "LIBRARIAN");
        if (!manager && !record.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(403, "无权取消他人的预约");
        }
        if (!(record.getStatus() == ReservationStatus.RESERVED || record.getStatus() == ReservationStatus.NOTIFIED)) {
            throw new BusinessException(409, "当前状态不可取消预约");
        }
        record.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toResponse(reservationRepository.save(record));
    }

    @Override
    @Transactional
    public void notifyNextReservation(Long bookId) {
        expireReservations();
        reservationRepository.findFirstByBookIdAndStatusAndExpireDateGreaterThanEqualOrderByReserveDateAsc(
                        bookId,
                        ReservationStatus.RESERVED,
                        LocalDate.now())
                .ifPresent(record -> {
                    record.setStatus(ReservationStatus.NOTIFIED);
                    record.setNotifyAt(LocalDateTime.now());
                    record.setExpireDate(LocalDate.now().plusDays(bookmsProperties.getBorrow().getNotifyExpireDays()));
                    reservationRepository.save(record);
                });
    }

    @Override
    @Transactional
    public void expireReservations() {
        List<ReservationRecord> expired = reservationRepository.findExpiredReservations(
                LocalDate.now(),
                List.of(ReservationStatus.RESERVED, ReservationStatus.NOTIFIED));
        if (expired.isEmpty()) {
            return;
        }
        expired.forEach(record -> record.setStatus(ReservationStatus.EXPIRED));
        reservationRepository.saveAll(expired);
    }

    private Specification<ReservationRecord> buildSpecification(Integer status, Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                ReservationStatus[] values = ReservationStatus.values();
                if (status < 0 || status >= values.length) {
                    throw new BusinessException(400, "无效的预约状态: " + status);
                }
                predicates.add(criteriaBuilder.equal(root.get("status"), values[status]));
            }
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}