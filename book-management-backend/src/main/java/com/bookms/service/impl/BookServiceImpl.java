package com.bookms.service.impl;

import com.bookms.dto.request.BookSaveRequest;
import com.bookms.dto.request.StockAdjustRequest;
import com.bookms.dto.response.BookResponse;
import com.bookms.dto.response.IsbnMetadataResponse;
import com.bookms.dto.response.PageResponse;
import com.bookms.entity.Book;
import com.bookms.entity.Category;
import com.bookms.entity.StockLog;
import com.bookms.entity.User;
import com.bookms.enums.BookStatus;
import com.bookms.enums.BorrowStatus;
import com.bookms.enums.ReservationStatus;
import com.bookms.enums.StockChangeType;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.BookMapper;
import com.bookms.repository.BookRepository;
import com.bookms.repository.BorrowRecordRepository;
import com.bookms.repository.CategoryRepository;
import com.bookms.repository.ReservationRepository;
import com.bookms.repository.StockLogRepository;
import com.bookms.repository.UserRepository;
import com.bookms.security.SecurityUtils;
import com.bookms.service.BookService;
import com.bookms.util.PageableUtils;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
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
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;
    private final StockLogRepository stockLogRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> pageBooks(String keyword, Long categoryId, Integer status, int page, int size, boolean publicView) {
        Pageable pageable = PageableUtils.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookResponse> result = bookRepository.findAll(buildSpecification(keyword, categoryId, status, publicView), pageable)
                .map(bookMapper::toResponse);
        return PageResponse.of(result);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBook(Long id, boolean publicView) {
        Book book = bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));
        if (publicView && book.getStatus() != BookStatus.ONLINE) {
            throw new BusinessException(404, "图书不存在");
        }
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional
    public BookResponse create(BookSaveRequest request) {
        validateIsbnForCreate(request.getIsbn());
        Category category = getCategory(request.getCategoryId());

        Book book = bookMapper.toEntity(request);
        Integer availableStock = request.getAvailableStock() == null ? request.getTotalStock() : request.getAvailableStock();
        validateStock(request.getTotalStock(), availableStock);
        book.setCategory(category);
        book.setAvailableStock(availableStock);
        book.setStatus(resolveStatus(request.getStatus()));
        Book saved = bookRepository.save(book);

        if (saved.getAvailableStock() > 0) {
            saveStockLog(saved, StockChangeType.INIT, saved.getAvailableStock(), 0, saved.getAvailableStock(), "图书初始化库存");
        }
        return bookMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookSaveRequest request) {
        Book book = getManagedBook(id);
        validateIsbnForUpdate(request.getIsbn(), id);
        Category category = getCategory(request.getCategoryId());

        int beforeAvailable = book.getAvailableStock();
        bookMapper.updateEntity(request, book);
        book.setCategory(category);
        book.setStatus(resolveStatus(request.getStatus()));
        int nextAvailable = request.getAvailableStock() == null ? book.getAvailableStock() : request.getAvailableStock();
        validateStock(request.getTotalStock(), nextAvailable);
        book.setAvailableStock(nextAvailable);

        Book saved = bookRepository.save(book);
        if (beforeAvailable != saved.getAvailableStock()) {
            saveStockLog(
                    saved,
                    StockChangeType.ADJUST,
                    saved.getAvailableStock() - beforeAvailable,
                    beforeAvailable,
                    saved.getAvailableStock(),
                    "更新图书库存");
        }
        return bookMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = getManagedBook(id);
        List<BorrowStatus> activeBorrowStatuses = List.of(
                BorrowStatus.APPLYING,
                BorrowStatus.APPROVED_PENDING_PICKUP,
                BorrowStatus.BORROWING,
                BorrowStatus.OVERDUE);
        if (borrowRecordRepository.existsByBookIdAndStatusIn(id, activeBorrowStatuses)) {
            throw new BusinessException(409, "图书存在未完成借阅，无法删除");
        }
        if (reservationRepository.existsByBookIdAndStatusIn(id, List.of(ReservationStatus.RESERVED, ReservationStatus.NOTIFIED))) {
            throw new BusinessException(409, "图书存在有效预约，无法删除");
        }
        bookRepository.delete(book);
    }

    @Override
    public IsbnMetadataResponse parseIsbn(String isbn) {
        return new IsbnMetadataResponse(
                isbn,
                "ISBN 图书示例《" + isbn + "》",
                "Mock Author",
                "Mock Publisher",
                "这是一个用于开发联调的 ISBN Mock 响应。",
                BigDecimal.valueOf(58.00));
    }

    @Override
    @Transactional
    public BookResponse adjustStock(Long id, StockAdjustRequest request) {
        if (request.getChangeQty() == null || request.getChangeQty() == 0) {
            throw new BusinessException(400, "changeQty 不能为 0");
        }
        Book book = bookRepository.findForUpdate(id).orElseThrow(() -> new BusinessException(404, "图书不存在"));
        int beforeAvailable = book.getAvailableStock();
        int beforeTotal = book.getTotalStock();
        int afterAvailable = beforeAvailable + request.getChangeQty();
        int afterTotal = beforeTotal + request.getChangeQty();
        validateStock(afterTotal, afterAvailable);

        book.setAvailableStock(afterAvailable);
        book.setTotalStock(afterTotal);
        Book saved = bookRepository.save(book);
        saveStockLog(saved, StockChangeType.ADJUST, request.getChangeQty(), beforeAvailable, afterAvailable, request.getRemark());
        return bookMapper.toResponse(saved);
    }

    private Specification<Book> buildSpecification(String keyword, Long categoryId, Integer status, boolean publicView) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), like)));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), resolveStatus(status)));
            } else if (publicView) {
                predicates.add(criteriaBuilder.equal(root.get("status"), BookStatus.ONLINE));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Book getManagedBook(Long id) {
        return bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "图书不存在"));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
    }

    private void validateIsbnForCreate(String isbn) {
        if (StringUtils.hasText(isbn) && bookRepository.existsByIsbnAndDeletedFalse(isbn)) {
            throw new BusinessException(409, "ISBN 已存在");
        }
    }

    private void validateIsbnForUpdate(String isbn, Long id) {
        if (StringUtils.hasText(isbn) && bookRepository.existsByIsbnAndDeletedFalseAndIdNot(isbn, id)) {
            throw new BusinessException(409, "ISBN 已存在");
        }
    }

    private void validateStock(int totalStock, int availableStock) {
        if (totalStock < 0 || availableStock < 0 || availableStock > totalStock) {
            throw new BusinessException(400, "库存数据不合法");
        }
    }

    private BookStatus resolveStatus(Integer status) {
        return status != null && status == 0 ? BookStatus.OFFLINE : BookStatus.ONLINE;
    }

    private void saveStockLog(Book book, StockChangeType type, int changeQty, int beforeQty, int afterQty, String remark) {
        StockLog stockLog = new StockLog();
        stockLog.setBook(book);
        stockLog.setChangeType(type.name().toLowerCase());
        stockLog.setChangeQty(changeQty);
        stockLog.setBeforeQty(beforeQty);
        stockLog.setAfterQty(afterQty);
        stockLog.setRemark(remark);
        Long operatorId = SecurityUtils.getCurrentUserId();
        User operator = userRepository.findByIdAndDeletedFalse(operatorId)
                .orElseThrow(() -> new BusinessException(404, "操作用户不存在"));
        stockLog.setOperator(operator);
        stockLogRepository.save(stockLog);
    }
}