package com.picky.domain.answer.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class AnswerRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class AnswerCreateRequestDTO {
        private String content;
        private Boolean isAI;
        private Long parentAnswerId; // 대댓글인 경우 부모 답변 ID
    }
}
