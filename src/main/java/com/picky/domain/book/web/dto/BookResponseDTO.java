package com.picky.domain.book.web.dto;

import com.picky.domain.book.entity.Book;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "책 응답 DTO")
public class BookResponseDTO{
    private Integer totalResults;
    private List<Item> item;

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Schema(description = "책 DTO")
    public static class BookDTO {

        @Schema(description = "책 제목", example = "동물농장")
        private String title;

        @Schema(description = "작가명", example = "조지 오웰")
        private List<String> authors;

        @Schema(description = "출판사명", example = "민음사")
        private String publisher;

        @Schema(description = "책 이미지", example = "https://naver.com/index.png")
        private String coverImage;

        @Schema(description = "ISBN 값", example = "9788934986225")
        private String isbn;

        @Schema(description = "출판 일자", example = "2025-03-12T07:45:20")
        private String publishedAt;

        public static BookDTO fromEntity(Book book) {
            return BookDTO.builder()
                    .title(book.getTitle())
                    .authors(Collections.singletonList(book.getAuthor()))
                    .publisher(book.getPublisher())
                    .coverImage(book.getCoverImage())
                    .isbn(book.getIsbn())
                    .publishedAt(String.valueOf(book.getPublishedAt()))
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "책 검색 응답 DTO")
    public static class BookSearchResponseDTO {
        private List<BookResponseDTO.BookDTO> items;
        private boolean hasNext;
    }

    @Data
    public static class Item {
        private String title;
        private String author;
        private String publisher;
        private String cover;
        private String isbn13;
        private String pubDate;
        private SubInfo subInfo;
        private String description;
    }

    @Data
    public static class SubInfo {
        private Integer itemPage;
    }

    @Getter
    @Setter
    @SuperBuilder
    @AllArgsConstructor
    @Schema(description = "책 상세 DTO")
    public static class BookDetailDTO extends BookDTO {

        @Schema(description = "페이지 수", example = "345")
        private Integer pageCount;

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

    @Getter
    @Setter
    @SuperBuilder
    @Schema(description = "내 서재 추가 응답 DTO")
    public static class BookSaveResponseDTO extends BookDetailDTO{
        @Schema(description = "서재 id", example = "1")
        private Long bookShelfId;

        public static BookSaveResponseDTO fromEntity(Book book) {
            return BookSaveResponseDTO.builder()
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

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "책 ID 조회 응답 DTO")
    public static class BookIdResponseDTO {

        @Schema(description = "책 ID", example = "1")
        private Long id;

        @Schema(description = "ISBN", example = "9788936434120")
        private String isbn;

        @Schema(description = "책 제목", example = "소년이 온다")
        private String title;

        @Schema(description = "저자", example = "한강")
        private String author;
    }


}
