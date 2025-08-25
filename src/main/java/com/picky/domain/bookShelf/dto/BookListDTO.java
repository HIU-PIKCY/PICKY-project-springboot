package com.picky.domain.bookShelf.dto;

import com.picky.domain.book.entity.Book;
import com.picky.domain.book.web.dto.BookDTO;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Collections;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "책 리스트 DTO")
public class BookListDTO {

    @Schema(description = "책 정보")
    private BookDTO book;

    @Schema(description = "독서 상태", example = "READING")
    private ReadingStatus readingStatus;

    @Schema(description = "책을 서재에 추가한 날짜", example = "2025-03-12T07:45:20")
    private LocalDateTime createdAt;

    public static BookListDTO fromBookShelf(BookShelf bookShelf) {
        Book book = bookShelf.getBook();
        return BookListDTO.builder()
                .book(BookDTO.fromEntity(bookShelf.getBook()))  // ✅ BookDTO로 감싸기
                .readingStatus(bookShelf.getReadingStatus())
                .createdAt(bookShelf.getCreatedAt())
                .build();
    }
}
