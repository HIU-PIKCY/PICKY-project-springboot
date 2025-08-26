package com.picky.domain.question.service;

import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionDetailResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionListResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.MyQuestionsResponseDTO;

public interface QuestionService {

    /**
     * 책에 대한 질문을 생성합니다.
     *
     * @param bookId 책 ID
     * @param memberId 질문을 작성하는 회원 ID
     * @param request 질문 생성 요청 DTO
     * @return 생성된 질문의 응답 DTO
     */
    QuestionPostResponseDTO createQuestion(Long bookId, Long memberId, QuestionPostRequestDTO request);

    /**
     * 질문 상세 정보를 조회합니다.
     *
     * @param questionId 질문 ID
     * @param memberId   요청하는 회원 ID (좋아요 여부 확인용)
     * @return 질문 상세 응답 DTO
     */
    QuestionDetailResponseDTO getQuestionDetail(Long questionId, Long memberId);

    /**
     * 특정 사용자가 작성한 질문 목록을 조회합니다.
     *
     * @param memberId 조회할 사용자 ID
     * @return 사용자가 작성한 질문 목록
     */
    MyQuestionsResponseDTO getMyQuestions(Long memberId);

    /**
     * 도서별 질문 목록을 조회합니다.
     * @param bookId
     * @return 질문 목록 응답 DTO
     */
    QuestionListResponseDTO getQuestionList(Long bookId);
}
