package com.picky.global.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "Book")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Book extends BaseEntity{

  @Schema(description = "책 제목", example = "동물농장")
  @Column(nullable = false, length = 256)
  private String title;

  @Schema(description = "작가명", example = "조지 오웰")
  @Column(nullable = false, length = 256)
  private String author;

  @Schema(description = "출판사명", example = "민음사")
  @Column(nullable = false, length = 256)
  private String publisher;

  @Schema(description = "책 이미지", example = "https://naver.com/index.png")
  @Column(columnDefinition = "TEXT")
  private String thumbnail;

  @Schema(description = "ISBN 값", example = "1346")
  private Long isbn;

  @Schema(description = "출판 일자", example = "2025-03-12T07:45:20")
  @Column()
  private LocalDateTime publishAt;
}
