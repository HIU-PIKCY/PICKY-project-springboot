package com.picky.domain.recommendation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@Schema(description = "키워드 기반 책 추천 응답")
public class RecommendationResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "키워드 기반 책 추천 응답")
    public static class BookRecommendationDTO {

        @Schema(description = "추천된 책 정보")
        private RecommendedBookInfo book;

        @Schema(description = "관련 질문 ID")
        private Long relatedQuestionId;

        @Schema(description = "추천 키워드", example = "가치관")
        private String recommendationKeyword;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "추천 책 정보")
    public static class RecommendedBookInfo {

        @Schema(description = "책 ID", example = "1")
        private Long id;

        @Schema(description = "책 제목", example = "소년이 온다")
        private String title;

        @Schema(description = "저자", example = "한강")
        private String author;

        @Schema(description = "표지 이미지")
        private String coverImage;

        @Schema(description = "ISBN")
        private String isbn;

        @Schema(description = "책 소개/설명",
                example = "1980년 5월 18일 광주. 차가운 주검으로 돌아온 친구를 보며 동호는 어둠 속에서 묻는다...")
        private String description;
    }
}
