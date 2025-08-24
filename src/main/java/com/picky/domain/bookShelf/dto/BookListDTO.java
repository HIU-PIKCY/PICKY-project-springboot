package com.picky.domain.bookShelf.dto;

import com.picky.domain.book.entity.Book;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "책 리스트 DTO")
public class BookListDTO {
    @Schema(description = "book id 값", example = "1")
    private Long id;

    @Schema(description = "책 제목", example = "동물농장")
    private String title;

    @Schema(description = "작가명", example = "조지 오웰")
    private List<String> author;

    @Schema(description = "책 이미지", example = "https://naver.com/index.png")
    private String coverImage;

    @Schema(description = "독서 상태", example = "READING")
    private ReadingStatus readingStatus;

    @Schema(description = "ISBN 값", example = "1346")
    private String isbn;

    public static BookListDTO fromBookShelf(BookShelf bookShelf) {
        Book book = bookShelf.getBook();
        return BookListDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(Collections.singletonList(book.getAuthor()))
                .coverImage(book.getThumbnail())
                .readingStatus(bookShelf.getReadingStatus())
                .isbn(book.getIsbn())
                .build();
    }
}
