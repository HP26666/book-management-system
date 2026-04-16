package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.BookCategory;
import com.bookms.entity.BookInfo;
import com.bookms.entity.BookStockLog;
import com.bookms.enums.BookStatus;
import com.bookms.enums.StockChangeType;
import com.bookms.exception.BusinessException;
import com.bookms.repository.BookCategoryRepository;
import com.bookms.repository.BookInfoRepository;
import com.bookms.repository.BookStockLogRepository;
import com.bookms.security.SecurityUtils;
import com.bookms.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookInfoRepository bookInfoRepository;
    private final BookCategoryRepository categoryRepository;
    private final BookStockLogRepository stockLogRepository;

    @Override
    public PageResponse<BookVO> listBooks(String keyword, Long categoryId, Integer status, Pageable pageable) {
        Page<BookInfo> page = bookInfoRepository.findByConditions(keyword, categoryId, status, pageable);
        return PageResponse.of(page.map(this::toBookVO));
    }

    @Override
    public BookVO getBookById(Long id) {
        BookInfo book = bookInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        return toBookVO(book);
    }

    @Override
    @Transactional
    public BookVO createBook(BookCreateDTO dto) {
        if (bookInfoRepository.findByIsbnAndDeletedFalse(dto.getIsbn()).isPresent()) {
            throw new BusinessException("ISBN已存在");
        }

        BookInfo book = new BookInfo();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setPublishDate(dto.getPublishDate());
        book.setPrice(dto.getPrice());
        book.setCategoryId(dto.getCategoryId());
        book.setSummary(dto.getSummary());
        book.setCoverUrl(dto.getCoverUrl());
        book.setLocation(dto.getLocation());
        book.setTotalStock(dto.getTotalStock());
        book.setAvailableStock(dto.getTotalStock());
        book.setStatus(BookStatus.ON_SHELF.getCode());
        bookInfoRepository.save(book);

        // Record stock log
        logStockChange(book, dto.getTotalStock(), StockChangeType.INIT, "新书入库");

        log.info("新增图书: {} (ISBN: {})", book.getTitle(), book.getIsbn());
        return toBookVO(book);
    }

    @Override
    @Transactional
    public BookVO updateBook(Long id, BookUpdateDTO dto) {
        BookInfo book = bookInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("图书不存在"));

        if (dto.getTitle() != null) book.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) book.setAuthor(dto.getAuthor());
        if (dto.getPublisher() != null) book.setPublisher(dto.getPublisher());
        if (dto.getPublishDate() != null) book.setPublishDate(dto.getPublishDate());
        if (dto.getPrice() != null) book.setPrice(dto.getPrice());
        if (dto.getCategoryId() != null) book.setCategoryId(dto.getCategoryId());
        if (dto.getSummary() != null) book.setSummary(dto.getSummary());
        if (dto.getCoverUrl() != null) book.setCoverUrl(dto.getCoverUrl());
        if (dto.getLocation() != null) book.setLocation(dto.getLocation());

        bookInfoRepository.save(book);
        return toBookVO(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        BookInfo book = bookInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        book.setDeleted(true);
        bookInfoRepository.save(book);
    }

    @Override
    @Transactional
    public void updateBookStatus(Long id, Integer status) {
        BookInfo book = bookInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        book.setStatus(status);
        bookInfoRepository.save(book);
    }

    @Override
    @Transactional
    public void adjustStock(Long id, StockAdjustDTO dto) {
        BookInfo book = bookInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("图书不存在"));

        int adjustCount = dto.getTotalStock() - book.getTotalStock();
        int newAvailable = book.getAvailableStock() + adjustCount;

        if (dto.getTotalStock() < 0) throw new BusinessException("调整后总库存不能为负数");
        if (newAvailable < 0) throw new BusinessException("调整后可用库存不能为负数");

        book.setTotalStock(dto.getTotalStock());
        book.setAvailableStock(newAvailable);
        bookInfoRepository.save(book);

        logStockChange(book, adjustCount, StockChangeType.ADJUST, dto.getRemark());
        log.info("调整图书库存: {} 调整量: {}", book.getTitle(), adjustCount);
    }

    private BookVO toBookVO(BookInfo book) {
        String categoryName = null;
        if (book.getCategoryId() != null) {
            categoryName = categoryRepository.findById(book.getCategoryId())
                    .map(BookCategory::getName).orElse(null);
        }

        return BookVO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .price(book.getPrice())
                .categoryId(book.getCategoryId())
                .categoryName(categoryName)
                .summary(book.getSummary())
                .coverUrl(book.getCoverUrl())
                .location(book.getLocation())
                .totalStock(book.getTotalStock())
                .availableStock(book.getAvailableStock())
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .build();
    }

    private void logStockChange(BookInfo book, int changeCount, StockChangeType type, String remark) {
        BookStockLog stockLog = new BookStockLog();
        stockLog.setBookId(book.getId());
        stockLog.setChangeType(type.name().toLowerCase());
        stockLog.setChangeQty(changeCount);
        stockLog.setBeforeQty(book.getTotalStock() - changeCount);
        stockLog.setAfterQty(book.getTotalStock());
        stockLog.setOperatorId(SecurityUtils.getCurrentUserIdOrNull());
        stockLogRepository.save(stockLog);
    }
}
