package com.picky.domain.book.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
