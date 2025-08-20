package com.picky.domain.book.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.swing.text.Document;
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
  private List<Document> documents;
  @Data
  public static class Document {
    private Long id;
    private String title;
    private List<String> authors;
    private String publisher;
    private String coverImage;
    private String isbn;
    private String datetime;
  }
}
