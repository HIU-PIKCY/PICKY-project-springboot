package com.picky.domain.home.web.dto;

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
}
