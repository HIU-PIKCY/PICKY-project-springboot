package com.picky.domain.book.web.dto;

import com.picky.domain.book.entity.Book;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "책 상세 DTO")
public class BookDetailDTO extends BookDTO {

    @Schema(description = "책이 서재에 추가되어 있는지 여부", example = "true")
    private Boolean isInLibrary = false;

    @Schema(description = "책이 서재에 추가 되어 있는 경우, 독서 상태", example = "READING")
    private ReadingStatus readingStatus = null;

    public static BookDetailDTO fromEntity(Book book) {
        return BookDetailDTO.builder()
                .title(book.getTitle())
                .authors(Collections.singletonList(book.getAuthor()))
                .publisher(book.getPublisher())
                .coverImage(book.getCoverImage())
                .isbn(book.getIsbn())
                .publishedAt(String.valueOf(book.getPublishedAt()))
                .pageCount(book.getPageCount())
                .build();
    }
}
