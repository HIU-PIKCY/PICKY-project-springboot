package com.picky.domain.book.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "책 DTO")
public class BookDTO {

  @Schema(description = "책 제목", example = "동물농장")
  private String title;

  @Schema(description = "작가명", example = "조지 오웰")
  private List<String> authors;

  @Schema(description = "출판사명", example = "민음사")
  private String publisher;

  @Schema(description = "책 이미지", example = "https://naver.com/index.png")
  private String coverImage;

  @Schema(description = "ISBN 값", example = "134611 12456")
  private String isbn;

  @Schema(description = "출판 일자", example = "2025-03-12T07:45:20")
  private String publishedAt;

  @Schema(description = "페이지 수", example = "345")
  private Integer pageCount;
}
