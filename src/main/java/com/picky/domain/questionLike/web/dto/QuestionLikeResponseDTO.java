package com.picky.domain.questionLike.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class QuestionLikeResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionLikeStatusResponseDTO { // 질문 좋아요/취소 상태 응답 DTO
        private Boolean isLiked; // 좋아요 상태
        private int likeCounts; // 해당 질문의 총 좋아요 수
    }

    // 사용자 좋아요 목록 조회용 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikeItemDTO {
        private Long id;               // 좋아요한 게시물 ID
        private String title;          // 게시물 제목
        private String author;         // 작성자
        private String time;           // 상대시간
        private int likes;             // 좋아요 수
        private int comments;          // 댓글 수
        private int views;             // 조회수
        private String type;           // 게시물 타입 ("question" | "answer")
        private Long originalId;       // 원본 게시물 ID
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyLikesResponseDTO {
        private List<LikeItemDTO> likes;
    }
}