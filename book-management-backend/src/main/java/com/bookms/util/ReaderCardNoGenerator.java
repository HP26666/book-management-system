package com.bookms.util;

import com.bookms.repository.ReaderRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReaderCardNoGenerator {

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    private final ReaderRepository readerRepository;

    public synchronized String next() {
        String prefix = "RD" + LocalDate.now().format(YEAR_FORMATTER);
        int nextSequence = readerRepository.findMaxSequenceByReaderCardPrefix(prefix) + 1;
        return prefix + String.format("%04d", nextSequence);
    }
}