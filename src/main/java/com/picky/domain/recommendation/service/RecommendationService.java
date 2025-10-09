package com.picky.domain.recommendation.service;
import com.picky.domain.recommendation.web.dto.RecommendationResponseDTO;
import com.picky.domain.recommendation.web.dto.RecommendationResponseDTO.BookRecommendationAnswerDTO;
import com.picky.domain.recommendation.web.dto.RecommendationResponseDTO.BookRecommendationDTO;

public interface RecommendationService {

        /**
         * 사용자 질문을 기반으로 책을 랜덤으로 응답합니다.
         *
         * @param memberId 로그인한 회원 ID
         * @return 책 응답 DTO
         */
        BookRecommendationDTO recommendBookBasedOnKeywords(Long memberId);

        /**
         * 피키 유저들이 읽은 책을 랜덤으로 응답합니다.
         * @param memberId 로그인한 회원 ID
         * @return 책 응답 DTO
         */
        RecommendationResponseDTO.RecommendedBookInfo recommendPickyPick(Long memberId);

    /**
     * 사용자 답변을 기반으로 책을 랜덤으로 응답합니다.
     *
     * @param memberId 로그인한 회원 ID
     * @return 책 응답 DTO
     */
    BookRecommendationAnswerDTO recommendBookBasedOnAnswers(Long memberId);
}
