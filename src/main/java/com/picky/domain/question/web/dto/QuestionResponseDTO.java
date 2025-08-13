package com.picky.domain.question.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

}
