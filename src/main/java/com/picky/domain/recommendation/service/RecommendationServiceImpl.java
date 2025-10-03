package com.picky.domain.recommendation.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.book.web.dto.BookResponseDTO;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.entity.QuestionKeyword;
import com.picky.domain.question.entity.enums.Keyword;
import com.picky.domain.question.repository.QuestionKeywordRepository;
import com.picky.domain.recommendation.web.dto.RecommendationResponseDTO.BookRecommendationDTO;
import com.picky.domain.recommendation.web.dto.RecommendationResponseDTO.RecommendedBookInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {
    private final Random random = new Random();
    private final QuestionKeywordRepository questionKeywordRepository;
    private final WebClient webClient;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    /**
     * 사용자의 질문 키워드를 기반으로 책을 추천합니다.
     * 추천 로직:
     * 1. 해당 사용자가 작성한 질문들의 키워드 중 가장 많이 사용된 키워드를 찾습니다.
     * 2. 그 키워드를 사용한 다른 사용자들의 질문 중 하나를 랜덤으로 선택합니다.
     * 3. 선택된 질문과 연결된 책을 추천합니다.
     */
    public BookRecommendationDTO recommendBookBasedOnKeywords(Long memberId) {
        log.info("[추천 시작] 사용자 ID: {}", memberId);

        // 1. 해당 사용자의 가장 많이 사용된 키워드 찾기
        List<QuestionKeyword> userKeywords = questionKeywordRepository
                .findByQuestionMemberId(memberId);

        if (userKeywords.isEmpty()) {
            log.warn("[추천 실패] 사용자 ID: {} - 작성한 질문이 없습니다.", memberId);
            throw new GeneralException(ErrorStatus.QUESTION_NOT_FOUND,
                    "추천을 위한 질문 데이터가 부족합니다. 질문을 작성해주세요.");
        }

        // 키워드 빈도수 계산
        Map<Keyword, Long> keywordCountMap = userKeywords.stream()
                .collect(Collectors.groupingBy(
                        QuestionKeyword::getKeyword,
                        Collectors.counting()
                ));

        // 가장 많이 사용된 키워드 찾기
        Keyword mostFrequentKeyword = keywordCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        Long keywordCount = keywordCountMap.get(mostFrequentKeyword);

        log.info("[키워드 분석] 가장 많이 사용된 키워드: '{}' ({}회 사용)",
                mostFrequentKeyword.getDisplayName(), keywordCount);

        // 2. 해당 키워드를 사용한 다른 사용자들의 질문 찾기
        List<QuestionKeyword> relatedKeywords = questionKeywordRepository
                .findByKeywordAndQuestionMemberIdNot(mostFrequentKeyword, memberId);

        if (relatedKeywords.isEmpty()) {
            log.warn("[추천 실패] 키워드: '{}' - 다른 사용자의 관련 질문이 없습니다.",
                    mostFrequentKeyword.getDisplayName());
            throw new GeneralException(ErrorStatus.QUESTION_NOT_FOUND,
                    "해당 키워드로 추천할 수 있는 책이 없습니다.");
        }

        // 질문 리스트 추출 (중복 제거)
        List<Question> relatedQuestions = relatedKeywords.stream()
                .map(QuestionKeyword::getQuestion)
                .filter(q -> q.getBook() != null) // 책이 있는 질문만
                .distinct()
                .collect(Collectors.toList());

        log.info("[질문 검색] 키워드 '{}' 관련 질문 {}개 발견",
                mostFrequentKeyword.getDisplayName(), relatedQuestions.size());

        // 3. 랜덤으로 하나의 질문 선택
        Question selectedQuestion = relatedQuestions.get(
                random.nextInt(relatedQuestions.size())
        );

        log.info("[추천 완료] 선택된 질문 ID: {}, 책: '{}'",
                selectedQuestion.getId(),
                selectedQuestion.getBook().getTitle());

        // 4. 알라딘 API에서 책 설명 가져오기
        String bookDescription = fetchBookDescription(selectedQuestion.getBook().getIsbn());

        // 4. DTO 생성 및 반환
        return BookRecommendationDTO.builder()
                .book(RecommendedBookInfo.builder()
                        .id(selectedQuestion.getBook().getId())
                        .title(selectedQuestion.getBook().getTitle())
                        .author(selectedQuestion.getBook().getAuthor())
                        .coverImage(selectedQuestion.getBook().getCoverImage())
                        .isbn(selectedQuestion.getBook().getIsbn())
                        .description(bookDescription)
                        .build())
                .relatedQuestionId(selectedQuestion.getId())
                .recommendationKeyword(mostFrequentKeyword.getDisplayName())
                .build();
    }

    /**
     * 알라딘 API를 통해 책 설명을 가져옵니다.
     */
    private String fetchBookDescription(String isbn) {
        try {
            String uri = "https://www.aladin.co.kr/ttb/api/ItemLookUp.aspx"
                    + "?ttbkey={apiKey}&itemIdType=ISBN&ItemId={isbn}&output=js&Version=20131101";

            BookResponseDTO response = webClient.get()
                    .uri(uri, aladinApiKey, isbn)
                    .retrieve()
                    .bodyToMono(BookResponseDTO.class)
                    .block();

            if (response != null && response.getItem() != null && !response.getItem().isEmpty()) {
                BookResponseDTO.Item item = response.getItem().get(0);
                String description = item.getDescription();

                // description이 있으면 반환, 없으면 null
                if (description != null && !description.isEmpty()) {
                    log.info("[알라딘 API] ISBN: {} - 책 설명 조회 성공", isbn);
                    return description;
                }
            }

            log.warn("[알라딘 API] ISBN: {} - 책 설명을 찾을 수 없습니다.", isbn);
            return null;

        } catch (Exception e) {
            log.error("[알라딘 API 오류] ISBN: {}, 에러: {}", isbn, e.getMessage());
            return null;
        }
    }
}
