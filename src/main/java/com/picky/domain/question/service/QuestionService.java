package com.picky.domain.question.service;

import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;

public interface QuestionService {

    /**
     * 책에 대한 질문을 생성합니다.
     *
     * @param bookId 책 ID
     * @return 생성된 질문의 응답 DTO
     */
    QuestionPostResponseDTO createQuestion(Long bookId, Long memberId, QuestionPostRequestDTO request);
}
