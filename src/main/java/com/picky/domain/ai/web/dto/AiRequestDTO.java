package com.picky.domain.ai.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "질문 생성을 위한 요청 객체")
public class AiRequestDTO {

    @Data
    public static class AiQuestionRequestDTO {
        @NotNull(message = "book id는 필수값입니다.")
        @Schema(description = "book id", example = "1")
        public Long bookId;

        @NotNull(message = "주제는 필수값입니다.")
        @Schema(description = "질문 생성을 위한 주제", example = "THEME")
        public String questionType;
    }
}

