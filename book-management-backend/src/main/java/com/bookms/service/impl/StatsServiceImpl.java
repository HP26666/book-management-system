package com.bookms.service.impl;

import com.bookms.dto.response.DashboardStatsResponse;
import com.bookms.enums.BorrowStatus;
import com.bookms.repository.BookRepository;
import com.bookms.repository.BorrowRecordRepository;
import com.bookms.repository.ReaderRepository;
import com.bookms.service.BorrowService;
import com.bookms.service.StatsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowService borrowService;

    @Override
    @Transactional
    public DashboardStatsResponse dashboard() {
        borrowService.refreshOverdueStatuses();
        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setTotalBooks(bookRepository.count());
        response.setTotalReaders(readerRepository.count());
        response.setMonthBorrowCount(borrowRecordRepository.countCreatedBetween(
                LocalDate.now().withDayOfMonth(1).atStartOfDay(),
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX)));
        response.setPendingApprovalCount(borrowRecordRepository.countByStatus(BorrowStatus.APPLYING));
        response.setPendingPickupCount(borrowRecordRepository.countByStatus(BorrowStatus.APPROVED_PENDING_PICKUP));
        response.setOverdueCount(borrowRecordRepository.countByStatus(BorrowStatus.OVERDUE));
        response.setBorrowTrend(buildTrend());
        response.setPopularBooks(buildPopularBooks());
        response.setBorrowStatusDistribution(buildStatusDistribution());
        return response;
    }

    private List<DashboardStatsResponse.TrendItem> buildTrend() {
        List<DashboardStatsResponse.TrendItem> trend = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(29);
        for (int i = 0; i < 30; i++) {
            LocalDate current = startDate.plusDays(i);
            long count = borrowRecordRepository.countCreatedBetween(
                    current.atStartOfDay(),
                    current.plusDays(1).atStartOfDay());
            trend.add(new DashboardStatsResponse.TrendItem(current.format(DATE_FORMATTER), count));
        }
        return trend;
    }

    private List<DashboardStatsResponse.PopularBookItem> buildPopularBooks() {
        return borrowRecordRepository.findPopularBooks(PageRequest.of(0, 10)).stream()
                .map(row -> new DashboardStatsResponse.PopularBookItem(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();
    }

    private List<DashboardStatsResponse.StatusItem> buildStatusDistribution() {
        List<DashboardStatsResponse.StatusItem> items = new ArrayList<>();
        for (BorrowStatus status : BorrowStatus.values()) {
            items.add(new DashboardStatsResponse.StatusItem(
                    status.getCode(),
                    status.getLabel(),
                    borrowRecordRepository.countByStatus(status)));
        }
        return items;
    }
}