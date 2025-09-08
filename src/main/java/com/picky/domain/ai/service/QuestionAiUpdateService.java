package com.picky.domain.ai.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.ai.service.AiService.AIResponseDTO;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// DB 업뎃 담당
@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionAiUpdateService {

    private final QuestionRepository questionRepository;

    @Transactional
    public void updateQuestionWithAiAnalysis(Long questionId, AIResponseDTO response) {

        Question question = questionRepository.findById(questionId)
                                              .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        question.updateAIAnalysis(response.summary(), response.hashtags());
    }
}
