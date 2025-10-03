package com.picky.domain.recommendation.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.question.entity.Question;
import com.picky.domain.recommendation.web.dto.RecommendationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {
    private final Random random = new Random();

    /**
     * 사용자의 질문 키워드를 기반으로 책을 추천합니다.
     *
     * 추천 로직:
     * 1. 해당 사용자가 작성한 질문들의 키워드 중 가장 많이 사용된 키워드를 찾습니다.
     * 2. 그 키워드를 사용한 다른 사용자들의 질문 중 하나를 랜덤으로 선택합니다.
     * 3. 선택된 질문과 연결된 책을 추천합니다.
     */
    public RecommendationResponseDTO recommendBookBasedOnKeywords(Long memberId) {
        log.info("[추천 시작] 사용자 ID: {}", memberId);

        // 1. 해당 사용자의 가장 많이 사용된 키워드 찾기
        List<Object[]> keywordStats = keywordService
                .findMostFrequentKeywordsByMemberId(memberId);

        if (keywordStats.isEmpty()) {
            log.warn("[추천 실패] 사용자 ID: {} - 작성한 질문이 없습니다.", memberId);
            throw new GeneralException(ErrorStatus.QUESTION_NOT_FOUND,
                    "추천을 위한 질문 데이터가 부족합니다. 질문을 작성해주세요.");
        }

        String mostFrequentKeyword = (String) keywordStats.get(0)[0];
        Long keywordCount = (Long) keywordStats.get(0)[1];

        log.info("[키워드 분석] 가장 많이 사용된 키워드: '{}' ({}회)",
                mostFrequentKeyword, keywordCount);

        // 2. 해당 키워드를 사용한 다른 사용자들의 질문 찾기
        List<Question> relatedQuestions = keywordService
                .findQuestionsByKeywordExcludingMember(mostFrequentKeyword, memberId);

        if (relatedQuestions.isEmpty()) {
            log.warn("[추천 실패] 키워드: '{}' - 관련 질문 없음", mostFrequentKeyword);
            throw new GeneralException(ErrorStatus.QUESTION_NOT_FOUND,
                    "해당 키워드로 추천할 수 있는 책이 없습니다.");
        }

        log.info("[질문 검색] 키워드 '{}' 관련 질문 {}개 발견",
                mostFrequentKeyword, relatedQuestions.size());

        // 3. 랜덤으로 하나의 질문 선택
        Question selectedQuestion = relatedQuestions.get(
                random.nextInt(relatedQuestions.size())
        );

        log.info("[추천 완료] 선택된 질문 ID: {}, 책: '{}'",
                selectedQuestion.getId(),
                selectedQuestion.getBook().getTitle());

        // 4. DTO 생성 및 반환
        return RecommendationResponseDTO.builder()
                .bookId(selectedQuestion.getBook().getId())
                .bookTitle(selectedQuestion.getBook().getTitle())
                .bookAuthor(selectedQuestion.getBook().getAuthor())
                .bookCover(selectedQuestion.getBook().getCoverImage())
                .isbn(selectedQuestion.getBook().getIsbn())
                .relatedQuestionId(selectedQuestion.getId())
                .recommendationKeyword(mostFrequentKeyword)
                .build();
    }
}
