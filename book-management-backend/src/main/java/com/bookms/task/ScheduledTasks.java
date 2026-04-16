package com.bookms.task;

import com.bookms.entity.BorrowRecord;
import com.bookms.entity.ReservationRecord;
import com.bookms.enums.BorrowStatus;
import com.bookms.enums.ReservationStatus;
import com.bookms.repository.BorrowRecordRepository;
import com.bookms.repository.ReservationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRecordRepository reservationRecordRepository;

    /**
     * 每天凌晨1点检查逾期借阅
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkOverdueBorrows() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository
                .findByStatusAndDueDateBeforeAndDeletedFalse(BorrowStatus.BORROWED.getCode(), LocalDate.now());

        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowStatus.OVERDUE.getCode());
            borrowRecordRepository.save(record);
        }

        if (!overdueRecords.isEmpty()) {
            log.info("定时任务：标记逾期借阅 {} 条", overdueRecords.size());
        }
    }

    /**
     * 每天凌晨2点处理过期预约
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processExpiredReservations() {
        List<ReservationRecord> expired = reservationRecordRepository
                .findByStatusAndExpireDateBeforeAndDeletedFalse(ReservationStatus.WAITING.getCode(), LocalDate.now());

        for (ReservationRecord record : expired) {
            record.setStatus(ReservationStatus.EXPIRED.getCode());
            reservationRecordRepository.save(record);
        }

        if (!expired.isEmpty()) {
            log.info("定时任务：处理过期预约 {} 条", expired.size());
        }
    }
}
