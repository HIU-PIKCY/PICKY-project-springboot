package com.picky.domain.answer.service;

import com.picky.domain.answer.web.dto.AnswerRequestDTO.AnswerCreateRequestDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerCreateResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerListResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.MyAnswersResponseDTO;

public interface AnswerService {

    /**
     * 특정 사용자가 작성한 답변 목록을 조회합니다.
     *
     * @param memberId 조회할 사용자 ID
     * @return 사용자가 작성한 답변 목록
     */
    MyAnswersResponseDTO getMyAnswers(Long memberId);

    /**
     * 특정 질문에 대한 답변을 등록합니다.
     *
     * @param questionId 답변을 등록할 질문 ID
     * @param memberId   답변 작성자 ID
     * @param request    답변 생성 요청 DTO
     * @return 생성된 답변 정보
     */
    AnswerCreateResponseDTO createAnswer(Long questionId, Long memberId, AnswerCreateRequestDTO request);

    /**
     * 특정 질문에 대한 모든 답변을 조회합니다.
     *
     * @param questionId 조회할 질문 ID
     * @return 해당 질문에 대한 답변 목록 DTO
     */
    AnswerListResponseDTO getAnswersByQuestion(Long questionId, Long memberId);

    /**
     * 특정 답변을 삭제합니다.
     *
     * @param answerId 삭제할 답변 ID
     * @param memberId 요청을 수행하는 사용자 ID
     */
    void deleteAnswer(Long answerId, Long memberId);
}
