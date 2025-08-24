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
    private Meta meta;
    private List<Document> documents;

    @Data
    public static class Meta {
        private int total_count;
        private int pageable_count;
        private boolean is_end;
    }

  @Data
  public static class Document {
    private String title;
    private List<String> authors;
    private String publisher;
    private String thumbnail;
    private String isbn;
    private String datetime;
  }
}
