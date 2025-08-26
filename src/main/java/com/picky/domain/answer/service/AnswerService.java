package com.picky.domain.answer.service;

import com.picky.domain.answer.web.dto.AnswerResponseDTO.MyAnswersResponseDTO;

public interface AnswerService {

    /**
     * 특정 사용자가 작성한 답변 목록을 조회합니다.
     *
     * @param memberId 조회할 사용자 ID
     * @return 사용자가 작성한 답변 목록
     */
    MyAnswersResponseDTO getMyAnswers(Long memberId);
}
