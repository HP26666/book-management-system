package com.bookms.repository;

import com.bookms.entity.Notice;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {

    @EntityGraph(attributePaths = {"publisher"})
    Optional<Notice> findByIdAndDeletedFalse(Long id);
}