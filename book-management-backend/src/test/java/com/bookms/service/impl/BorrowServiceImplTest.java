package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.*;
import com.bookms.enums.*;
import com.bookms.exception.BusinessException;
import com.bookms.repository.*;
import com.bookms.security.UserPrincipal;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceImplTest {

    @InjectMocks
    private BorrowServiceImpl borrowService;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookInfoRepository bookInfoRepository;

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private ReaderInfoRepository readerInfoRepository;

    private BookInfo sampleBook;
    private ReaderInfo sampleReader;

    @BeforeEach
    void setUp() {
        sampleBook = new BookInfo();
        sampleBook.setId(1L);
        sampleBook.setTitle("测试图书");
        sampleBook.setTotalStock(10);
        sampleBook.setAvailableStock(5);
        sampleBook.setStatus(BookStatus.ON_SHELF.getCode());
        sampleBook.setDeleted(false);

        sampleReader = new ReaderInfo();
        sampleReader.setId(1L);
        sampleReader.setUserId(100L);
        sampleReader.setMaxBorrowCount(5);
        sampleReader.setCurrentBorrowCount(2);
        sampleReader.setIsBlacklist(false);
        sampleReader.setCreditScore(100);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(Long userId) {
        UserPrincipal principal = new UserPrincipal(userId, "testuser", List.of("READER"));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("申请借阅 - 图书不存在抛异常")
    void applyBorrow_bookNotFound() {
        setSecurityContext(100L);
        when(readerInfoRepository.findByUserId(100L)).thenReturn(Optional.of(sampleReader));
        when(bookInfoRepository.findById(999L)).thenReturn(Optional.empty());

        BorrowApplyDTO dto = new BorrowApplyDTO();
        dto.setBookId(999L);
        dto.setBorrowDays(30);

        assertThatThrownBy(() -> borrowService.applyBorrow(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("申请借阅 - 无库存抛异常")
    void applyBorrow_noStock() {
        setSecurityContext(100L);
        sampleBook.setAvailableStock(0);
        when(readerInfoRepository.findByUserId(100L)).thenReturn(Optional.of(sampleReader));
        when(bookInfoRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BorrowApplyDTO dto = new BorrowApplyDTO();
        dto.setBookId(1L);
        dto.setBorrowDays(30);

        assertThatThrownBy(() -> borrowService.applyBorrow(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("续借 - 记录不存在抛异常")
    void renewBorrow_notFound() {
        when(borrowRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.renewBorrow(999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("续借 - 超过续借次数限制")
    void renewBorrow_exceedLimit() {
        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setStatus(BorrowStatus.BORROWED.getCode());
        record.setRenewCount(1);
        record.setDueDate(LocalDate.now().plusDays(5));

        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> borrowService.renewBorrow(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("审批借阅 - 批准")
    void approveBorrow_success() {
        setSecurityContext(200L);

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setBookId(1L);
        record.setUserId(100L);
        record.setStatus(BorrowStatus.PENDING.getCode());

        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(bookInfoRepository.decrementStock(1L)).thenReturn(1);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(i -> i.getArgument(0));

        borrowService.approveBorrow(1L);

        verify(borrowRecordRepository).save(argThat(r ->
                r.getStatus().equals(BorrowStatus.BORROWED.getCode()) && r.getBorrowDate() != null));
    }

    @Test
    @DisplayName("审批借阅 - 非待审批状态抛异常")
    void approveBorrow_wrongStatus() {
        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setStatus(BorrowStatus.BORROWED.getCode());

        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> borrowService.approveBorrow(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("拒绝借阅")
    void rejectBorrow_success() {
        setSecurityContext(200L);

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setBookId(1L);
        record.setStatus(BorrowStatus.PENDING.getCode());

        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(i -> i.getArgument(0));

        borrowService.rejectBorrow(1L, "库存不足");

        verify(borrowRecordRepository).save(argThat(r ->
                r.getStatus().equals(BorrowStatus.REJECTED.getCode()) && "库存不足".equals(r.getRejectReason())));
    }

    @Test
    @DisplayName("统计逾期数量")
    void countOverdue() {
        when(borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.OVERDUE.getCode())).thenReturn(5L);

        long count = borrowService.countOverdue();

        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("统计待审批数量")
    void countPendingApprovals() {
        when(borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.PENDING.getCode())).thenReturn(3L);

        long count = borrowService.countPendingApprovals();

        assertThat(count).isEqualTo(3);
    }
}
