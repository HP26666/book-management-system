package com.bookms.repository;

import com.bookms.entity.Book;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbnAndDeletedFalse(String isbn);

    boolean existsByIsbnAndDeletedFalseAndIdNot(String isbn, Long id);

    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    Optional<Book> findByIdAndDeletedFalse(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b left join fetch b.category where b.id = :id")
    Optional<Book> findForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.availableStock = b.availableStock - 1, b.updatedAt = CURRENT_TIMESTAMP where b.id = :bookId and b.availableStock > 0")
    int decreaseAvailableStock(@Param("bookId") Long bookId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.availableStock = b.availableStock + 1, b.updatedAt = CURRENT_TIMESTAMP where b.id = :bookId and b.availableStock < b.totalStock")
    int increaseAvailableStock(@Param("bookId") Long bookId);
}