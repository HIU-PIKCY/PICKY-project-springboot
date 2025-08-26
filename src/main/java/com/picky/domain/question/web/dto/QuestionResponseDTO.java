package com.picky.domain.question.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QuestionResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionPostResponseDTO { // 질문 등록 응답 DTO
        private Long id;
        private String title;
        private String content;
        private int page;
        private Boolean isAI;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDetailResponseDTO { // 질문 상세 응답 DTO
        private Long id;
        private String title;
        private String content;
        private String author; // 멤버 (질문 작성자)
        private Boolean isAI;
        private int views;
        private int likes;
        private int answersCount;
        private int page;
        private LocalDateTime createdAt;
        private BookInfoResponseDTO book; // 관련 책 정보
        private Boolean isLiked; // 사용자가 좋아요를 눌렀는지 여부
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookInfoResponseDTO {
        private Long id;
        private String title;
        private String author;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionListResponseDTO { // 질문 목록 응답 DTO
        private List<QuestionInfoResponseDTO> questions; // 질문 목록
        private int totalCount; // 전체 질문 수
        private boolean hasMore; // 추가 데이터 존재 여부
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionInfoResponseDTO {
        private Long id;
        private String title;
        private String content;
        private int views;
        private int likes; // 좋아요 수
        private int answersCount;
        private Boolean isAI;
        private int page;
        private LocalDateTime createdAt;
    }

    // 사용자 질문 목록 조회
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyQuestionDTO {
        private Long id;
        private String title;
        private String author;
        private String book;
        private int likes;
        private int comments;
        private int views;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyQuestionsResponseDTO {
        private List<MyQuestionDTO> questions;
    }
}
