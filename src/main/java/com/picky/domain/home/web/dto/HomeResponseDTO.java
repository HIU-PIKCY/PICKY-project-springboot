package com.picky.domain.home.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class HomeResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HotTopicResponseDTO { // 이번 주 핫 토픽 응답
        private Long questionId;

        private String bookTitle;
        private String bookAuthor;
        private String bookCover;

        private String questionTitle;
        private String aiSummary;
        private List<String> hashtags;

        private String weekInfo; // "9월 1주차" 같은 정보

        private int likes;
        private int comments;
        private int views;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MostQuestionedBooksResponseDTO { // 질문이 가장 많은 책 응답
        private List<MostQuestionedBookResponseDTO> books;
        private String weekInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MostQuestionedBookResponseDTO { // 질문이 가장 많은 책 응답
        private Long bookId;
        private String bookTitle;
        private String bookAuthor;
        private String bookCover;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyKeywordResponseDTO {

        @Schema(description = "이번 주 키워드 정보 (1~3위)")
        private List<KeywordInfo> keywords;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class KeywordInfo {
            @Schema(description = "순위", example = "1")
            private int rank;

            @Schema(description = "키워드", example = "성장")
            private String keyword;
        }
    }
}
