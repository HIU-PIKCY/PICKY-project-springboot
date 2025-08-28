package com.picky.domain.answer.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class AnswerResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyAnswerDTO {
        private Long id;               // 답변 고유 ID
        private String title;          // 답변 내용
        private String questionTitle;  // 원본 질문 제목
        private Long questionId;       // 원본 질문 ID
        private int views;             // 원본 질문의 조회수
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyAnswersResponseDTO {
        private List<MyAnswerDTO> answers;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerCreateResponseDTO {
        private Long id;
        private String content;
        private String author; // 작성자 이름
        private Boolean isAI;
        private LocalDateTime createdAt;
    }
}