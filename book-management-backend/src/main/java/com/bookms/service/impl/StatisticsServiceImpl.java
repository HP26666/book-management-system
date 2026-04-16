package com.bookms.service.impl;

import com.bookms.dto.DashboardVO;
import com.bookms.enums.BookStatus;
import com.bookms.enums.BorrowStatus;
import com.bookms.repository.BookInfoRepository;
import com.bookms.repository.BorrowRecordRepository;
import com.bookms.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BookInfoRepository bookInfoRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    public DashboardVO getDashboard() {
        long totalBooks = bookInfoRepository.countByDeletedFalse();
        long onShelfBooks = bookInfoRepository.countByStatusAndDeletedFalse(BookStatus.ON_SHELF.getCode());
        long totalBorrows = borrowRecordRepository.count();

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        long monthBorrows = borrowRecordRepository.countByBorrowDateAfter(monthStart);

        long pendingApprovals = borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.PENDING.getCode());
        long overdueCount = borrowRecordRepository.countByStatusAndDeletedFalse(BorrowStatus.OVERDUE.getCode());

        // Hot books
        List<Object[]> topBorrowed = borrowRecordRepository.findTopBorrowedBooks(
                org.springframework.data.domain.PageRequest.of(0, 10));
        List<DashboardVO.HotBookVO> hotBooks = new ArrayList<>();
        for (Object[] row : topBorrowed) {
            Long bookId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            bookInfoRepository.findById(bookId).ifPresent(book ->
                    hotBooks.add(DashboardVO.HotBookVO.builder()
                            .bookId(bookId)
                            .title(book.getTitle())
                            .author(book.getAuthor())
                            .borrowCount(count)
                            .build())
            );
        }

        // Monthly trend (last 6 months)
        List<Object[]> trendData = borrowRecordRepository.monthlyBorrowTrend(
                LocalDate.now().minusMonths(6), LocalDate.now());
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        for (Object[] row : trendData) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", row[0].toString() + "-" + String.format("%02d", ((Number) row[1]).intValue()));
            item.put("count", ((Number) row[2]).longValue());
            monthlyTrend.add(item);
        }

        return DashboardVO.builder()
                .totalBooks(totalBooks)
                .onShelfBooks(onShelfBooks)
                .totalBorrows(totalBorrows)
                .monthBorrows(monthBorrows)
                .pendingApprovals(pendingApprovals)
                .overdueCount(overdueCount)
                .hotBooks(hotBooks)
                .monthlyTrend(monthlyTrend)
                .build();
    }
}
