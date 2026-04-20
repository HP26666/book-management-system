package com.bookms.repository;

import com.bookms.entity.Reader;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReaderRepository extends JpaRepository<Reader, Long>, JpaSpecificationExecutor<Reader> {

    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<Reader> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<Reader> findByUserIdAndDeletedFalse(Long userId);

    boolean existsByUserIdAndDeletedFalse(Long userId);

    @Query(value = "select coalesce(max(cast(right(reader_card_no, 4) as integer)), 0) from reader_info where reader_card_no like concat(:prefix, '%')", nativeQuery = true)
    Integer findMaxSequenceByReaderCardPrefix(@Param("prefix") String prefix);
}