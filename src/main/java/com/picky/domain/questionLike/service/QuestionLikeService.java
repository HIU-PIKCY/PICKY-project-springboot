package com.picky.domain.questionLike.service;

import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.QuestionLikeStatusResponseDTO;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.MyLikesResponseDTO;

public interface QuestionLikeService {

    /**
     * 특정 질문에 좋아요를 추가/삭제합니다.
     * @param questionId 질문 ID
     * @param memberId  회원 ID
     * @return 질문 좋아요 상태 응답 DTO
     */
    QuestionLikeStatusResponseDTO likeQuestion(Long questionId, Long memberId);

    /**
     * 특정 사용자가 좋아요한 목록을 조회합니다.
     * @param memberId 회원 ID
     * @return 좋아요 목록 응답 DTO
     */
    MyLikesResponseDTO getMyLikes(Long memberId);
}