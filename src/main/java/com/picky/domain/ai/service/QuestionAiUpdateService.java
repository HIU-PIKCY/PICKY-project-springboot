package com.picky.domain.ai.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.ai.service.AiService.AIResponseDTO;
import com.picky.domain.ai.web.dto.AiRequestDTO.AiQuestionRequestDTO;
import com.picky.domain.answer.entity.Answer;
import com.picky.domain.answer.repository.AnswerRepository;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.member.entity.Member;
import com.picky.domain.question.entity.AiHashtag;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.entity.QuestionKeyword;
import com.picky.domain.question.entity.enums.Keyword;
import com.picky.domain.question.entity.enums.QuestionType;
import com.picky.domain.question.repository.QuestionKeywordRepository;
import com.picky.domain.question.repository.QuestionRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// DB 업뎃 담당
@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionAiUpdateService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final BookRepository bookRepository;
    private final QuestionKeywordRepository questionKeywordRepository;

    @Transactional
    public Question saveGeneratedQuestion(AiQuestionRequestDTO request, Member member, AiService.GeneratedQuestionDTO dto) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND));

        QuestionType questionType = QuestionType.valueOf(request.getQuestionType().toUpperCase());

        Question question = Question.builder()
                .book(book)
                .member(member)
                .title(dto.title())
                .content(dto.content())
                .isAiGenerated(true)
                .type(questionType)
                .build();

        return questionRepository.save(question);
    }

    @Transactional
    public Answer saveGeneratedAnswer(Long questionId, Member member, AiService.GeneratedAnswerDTO dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Answer answer = Answer.builder()
                .question(question)
                .member(member)
                .content(dto.content())
                .isAiGenerated(true)
                .build();

        return answerRepository.save(answer);
    }

    @Async
    @Transactional
    public void saveKeywords(Long questionId, List<String> keywordStrings) {
        Question question = questionRepository.findById(questionId)
                                              .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        if (keywordStrings != null) {
            List<QuestionKeyword> questionKeywords = keywordStrings.stream()
                                                                   .map(keywordStr -> {
                                                                       // Keyword Enum에 추가한 헬퍼 메서드 사용
                                                                       Keyword keywordEnum = Keyword.fromPromptValue(keywordStr);
                                                                       if (keywordEnum != null) {
                                                                           return QuestionKeyword.builder()
                                                                                                 .question(question)
                                                                                                 .keyword(keywordEnum)
                                                                                                 .build();
                                                                       }
                                                                       return null;
                                                                   })
                                                                   .filter(Objects::nonNull)
                                                                   .collect(Collectors.toList());

            questionKeywordRepository.saveAll(questionKeywords);
            log.info("[Async] 질문 ID {} 에 대한 키워드 저장 완료", questionId);
        }
    }

    @Transactional
    public void updateQuestionWithAiAnalysis(Long questionId, AIResponseDTO response) {
        Question question = questionRepository.findById(questionId)
                                              .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        List<AiHashtag> hashtags = Arrays.stream(response.hashtags().split(","))
                                         .map(String::trim)
                                         .map(tag -> AiHashtag.builder().question(question).tag(tag).build())
                                         .collect(Collectors.toList());

        question.updateAIAnalysis(response.summary(), hashtags);
    }
}
