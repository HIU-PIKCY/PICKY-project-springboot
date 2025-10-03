package com.picky.domain.recommendation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "키워드 기반 책 추천 응답")
public class RecommendationResponseDTO {

    @Schema(description = "추천된 책 ID", example = "1")
    private Long bookId;

    @Schema(description = "책 제목", example = "소년이 온다")
    private String bookTitle;

    @Schema(description = "저자", example = "한강")
    private String bookAuthor;

    @Schema(description = "표지 이미지")
    private String bookCover;

    @Schema(description = "ISBN")
    private String isbn;

    @Schema(description = "관련 질문 ID")
    private Long relatedQuestionId;

    @Schema(description = "추천 키워드", example = "성장")
    private String recommendationKeyword;
}
