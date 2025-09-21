package com.picky.domain.ai.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.ai.service.AiService.AIResponseDTO;
import com.picky.domain.ai.web.dto.AiQuestionRequestDTO;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Transactional
    public void updateQuestionWithAiAnalysis(Long questionId, AIResponseDTO response) {

        Question question = questionRepository.findById(questionId)
                                              .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        question.updateAIAnalysis(response.summary(), response.hashtags());
    }

    @Transactional
    public Question saveGeneratedQuestion(AiQuestionRequestDTO request, Member member, AiService.GeneratedQuestionDTO dto) {
        Book book = bookRepository.findById(request.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND));

        Question question = Question.builder()
                .book(book)
                .member(member)
                .title(dto.title())
                .content(dto.content())
                .isAiGenerated(true) // AI 생성된 질문임을 표시
                .build();

        return questionRepository.save(question);
    }
}
