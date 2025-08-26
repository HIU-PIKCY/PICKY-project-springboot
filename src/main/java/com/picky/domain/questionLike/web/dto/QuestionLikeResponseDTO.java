package com.picky.domain.questionLike.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QuestionLikeResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionLikeStatusResponseDTO { // 질문 좋아요/취소 상태 응답 DTO
        private Boolean isLiked; // 좋아요 상태
        private int likeCounts; // 해당 질문의 총 좋아요 수
    }
}
