package com.picky.domain.question.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QuestionRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class QuestionPostRequestDTO {

        @NotBlank(message = "제목은 필수입니다.")
        @Schema(description = "제목", example = "질문제목")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        @Schema(description = "내용", example = "민음사")
        private String content;

        @Schema(description = "관련 페이지", example = "46")
        private Integer page;

        @Schema(description = "AI 생성 여부", example = "false")
        private Boolean isAI;
    }
}
