package com.picky.domain.question.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

public class QuestionResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionPostResponseDTO {
        private Long id;

        private String title;

        private String content;

        private Integer page;

        private Boolean isAI;

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
        private Integer likes;
        private Integer comments;
        private Integer views;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyQuestionsResponseDTO {
        private List<MyQuestionDTO> questions;
    }
}
