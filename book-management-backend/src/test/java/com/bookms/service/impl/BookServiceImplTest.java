package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.*;
import com.bookms.enums.*;
import com.bookms.exception.BusinessException;
import com.bookms.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookInfoRepository bookInfoRepository;

    @Mock
    private BookCategoryRepository bookCategoryRepository;

    @Mock
    private BookStockLogRepository bookStockLogRepository;

    private BookInfo sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new BookInfo();
        sampleBook.setId(1L);
        sampleBook.setIsbn("978-7-111-11111-1");
        sampleBook.setTitle("Java编程思想");
        sampleBook.setAuthor("Bruce Eckel");
        sampleBook.setPublisher("机械工业出版社");
        sampleBook.setCategoryId(1L);
        sampleBook.setTotalStock(10);
        sampleBook.setAvailableStock(8);
        sampleBook.setStatus(BookStatus.ON_SHELF.getCode());
        sampleBook.setPrice(BigDecimal.valueOf(108.00));
        sampleBook.setLocation("A-01-01");
        sampleBook.setDeleted(false);
    }

    @Test
    @DisplayName("根据ID获取图书 - 成功")
    void getBookById_success() {
        when(bookInfoRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookVO result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java编程思想");
        assertThat(result.getAuthor()).isEqualTo("Bruce Eckel");
    }

    @Test
    @DisplayName("根据ID获取图书 - 不存在时抛异常")
    void getBookById_notFound() {
        when(bookInfoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("创建图书 - 成功")
    void createBook_success() {
        BookCreateDTO dto = new BookCreateDTO();
        dto.setIsbn("978-7-222-22222-2");
        dto.setTitle("Spring实战");
        dto.setAuthor("Craig Walls");
        dto.setPublisher("人民邮电出版社");
        dto.setCategoryId(1L);
        dto.setTotalStock(5);
        dto.setPrice(BigDecimal.valueOf(89.00));

        when(bookInfoRepository.findByIsbnAndDeletedFalse("978-7-222-22222-2")).thenReturn(Optional.empty());
        when(bookInfoRepository.save(any(BookInfo.class))).thenAnswer(i -> {
            BookInfo saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        BookVO result = bookService.createBook(dto);

        assertThat(result).isNotNull();
        verify(bookInfoRepository).save(any(BookInfo.class));
        verify(bookStockLogRepository).save(any(BookStockLog.class));
    }

    @Test
    @DisplayName("删除图书 - 软删除")
    void deleteBook_success() {
        when(bookInfoRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookInfoRepository.save(any(BookInfo.class))).thenReturn(sampleBook);

        bookService.deleteBook(1L);

        verify(bookInfoRepository).save(argThat(BookInfo::getDeleted));
    }

    @Test
    @DisplayName("修改图书状态 - 上下架")
    void updateBookStatus_success() {
        when(bookInfoRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookInfoRepository.save(any(BookInfo.class))).thenReturn(sampleBook);

        bookService.updateBookStatus(1L, BookStatus.OFF_SHELF.getCode());

        verify(bookInfoRepository).save(argThat(book ->
                book.getStatus().equals(BookStatus.OFF_SHELF.getCode())));
    }

    @Test
    @DisplayName("分页查询图书")
    void listBooks_success() {
        Page<BookInfo> page = new PageImpl<>(List.of(sampleBook), PageRequest.of(0, 10), 1);
        when(bookInfoRepository.findByConditions(eq("Java"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<BookVO> result = bookService.listBooks("Java", null, null, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("调整库存 - 增加")
    void adjustStock_increase() {
        sampleBook.setTotalStock(10);
        sampleBook.setAvailableStock(8);
        when(bookInfoRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookInfoRepository.save(any(BookInfo.class))).thenAnswer(i -> i.getArgument(0));

        StockAdjustDTO dto = new StockAdjustDTO();
        dto.setTotalStock(15);
        dto.setRemark("补充库存");

        bookService.adjustStock(1L, dto);

        verify(bookInfoRepository).save(argThat(book ->
                book.getTotalStock() == 15 && book.getAvailableStock() == 13));
    }
}
