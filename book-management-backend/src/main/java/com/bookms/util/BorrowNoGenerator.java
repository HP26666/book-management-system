package com.bookms.util;

import com.bookms.repository.BorrowRecordRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BorrowNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BorrowRecordRepository borrowRecordRepository;

    public synchronized String next() {
        String prefix = "B" + LocalDate.now().format(DATE_FORMATTER);
        int nextSequence = borrowRecordRepository.findMaxSequenceByBorrowNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", nextSequence);
    }
}