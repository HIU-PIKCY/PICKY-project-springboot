package com.picky.domain.answer.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class AnswerRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class AnswerCreateRequestDTO {
        private String content;
        private Boolean isAI;
    }
}
