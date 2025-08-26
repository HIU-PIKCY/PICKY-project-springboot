package com.picky.domain.questionLike.service;

import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.QuestionLikeStatusResponseDTO;

public interface QuestionLikeService {

    /**
     * 특정 질문에 좋아요를 추가/삭제합니다.
     * @param questionId 질문 ID
     * @param memberId  회원 ID
     * @return 질문 좋아요 상태 응답 DTO
     */
    QuestionLikeStatusResponseDTO likeQuestion(Long questionId, Long memberId);
}
