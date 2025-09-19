package com.picky.domain.bookShelf.web.dto;

import com.picky.domain.book.web.dto.BookResponseDTO.BookDTO;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "책 리스트 DTO")
public class BookShelfResponseDTO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "책 정보")
    private BookDTO book;

    @Schema(description = "독서 상태", example = "READING")
    private ReadingStatus readingStatus;

    @Schema(description = "책을 서재에 추가한 날짜", example = "2025-03-12T07:45:20")
    private LocalDateTime createdAt;

    public static BookShelfResponseDTO fromBookShelf(BookShelf bookShelf) {
        return BookShelfResponseDTO.builder()
                .id(bookShelf.getId())
                .book(BookDTO.fromEntity(bookShelf.getBook()))
                .readingStatus(bookShelf.getReadingStatus())
                .createdAt(bookShelf.getCreatedAt())
                .build();
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "서재 검색 응답 DTO (무한 스크롤용)")
    public static class GetBookShelfResponseDTO {
        private List<BookShelfResponseDTO> items;
        private boolean hasNext;
    }
}


