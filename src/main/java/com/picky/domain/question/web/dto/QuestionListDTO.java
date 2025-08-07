package com.picky.domain.question.web.dto;

import com.picky.domain.member.web.dto.BaseUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Schema(description = "질문 리스트 조회를 위한 질문 DTO")
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListDTO {
  @Schema(example = "1", description = "질문 pk값")
  private Long pk;

  @Schema(example = "질문 뭐라 하지", description = "내용")
  private String content;

  @Schema(example = "46", description = "페이지 수")
  private String pageNum;

  @Schema(example = "false", description = "AI 생성 여부")
  private String isAiGenerated;

  private BaseUserDTO user;

  @Schema(example = "1", description = "책 pk값")
  private Long bookPk;

  @Schema(example = "2025-03-12T07:45:20", description = "생성 시각")
  private LocalDateTime createdAt;

  @Schema(example = "3", description = "좋아요 수")
  private Integer likes;
}
