package com.picky.domain.myBook.entity;

import com.picky.domain.book.entity.Book;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.myBook.entity.enums.ReadingStatus;
import com.picky.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MyBook extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_pk", foreignKey = @ForeignKey(name = "fk_my_book_book"))
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_shelf_id", foreignKey = @ForeignKey(name = "fk_my_book_book_shelf"))
    private BookShelf bookShelf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReadingStatus readingStatus = ReadingStatus.READING;
}
