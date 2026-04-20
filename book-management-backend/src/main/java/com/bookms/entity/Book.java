package com.bookms.entity;

import com.bookms.enums.BookStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "book_info")
@SQLDelete(sql = "UPDATE book_info SET deleted = true, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class Book extends SoftDeleteEntity {

    @Column(length = 20)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String author;

    @Column(length = 100)
    private String publisher;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock = 0;

    @Column(name = "available_stock", nullable = false)
    private Integer availableStock = 0;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private BookStatus status = BookStatus.ONLINE;

    @Column(length = 100)
    private String location;
}