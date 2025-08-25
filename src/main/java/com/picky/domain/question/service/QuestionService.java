package com.picky.domain.question.service;

import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.MyQuestionsResponseDTO;

public interface QuestionService {

    /**
     * 책에 대한 질문을 생성합니다.
     *
     * @param bookId 책 ID
     * @return 생성된 질문의 응답 DTO
     */
    QuestionPostResponseDTO createQuestion(Long bookId, Long memberId, QuestionPostRequestDTO request);

    /**
     * 특정 사용자가 작성한 질문 목록을 조회합니다.
     *
     * @param memberId 조회할 사용자 ID
     * @return 사용자가 작성한 질문 목록
     */
    MyQuestionsResponseDTO getMyQuestions(Long memberId);
}
