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
    private Long totalItems;
    private List<Volume> items;

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

        @Schema(description = "페이지 수", example = "345")
        private Integer pageCount;

        public static BookDTO fromEntity(Book book) {
            return BookDTO.builder()
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
    @AllArgsConstructor
    @Schema(description = "책 상세 DTO")
    public static class BookDetailDTO extends BookDTO {

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
    @NoArgsConstructor
    public static class Volume {
        private VolumeInfo volumeInfo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VolumeInfo {
        private String title;
        private List<String> authors;
        private String publisher;
        private String publishedDate;
        private ImageLinks imageLinks;
        private Integer pageCount;  // 총 페이지 수
        private List<IndustryIdentifiers> industryIdentifiers;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ImageLinks {
        private String smallThumbnail;
        private String thumbnail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class IndustryIdentifiers {
        private String type;
        private String identifier;
    }

}
