package com.bookms.repository;

import com.bookms.entity.BookStockLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookStockLogRepository extends JpaRepository<BookStockLog, Long> {
}
