package com.picky.domain.home.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.book.entity.Book;
import com.picky.domain.home.web.dto.HomeResponseDTO.HotTopicResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.MostQuestionedBookResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.MostQuestionedBooksResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.WeeklyKeywordResponseDTO;
import com.picky.domain.question.entity.AiHashtag;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionKeywordRepository;
import com.picky.domain.question.repository.QuestionRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HomeServiceImpl implements HomeService {

    private final QuestionRepository questionRepository;
    private final QuestionKeywordRepository questionKeywordRepository;

    @Override
    public HotTopicResponseDTO getHotTopic() {

        DateRange lastWeek = getLastWeekRange();
        String weekInfo = getWeekInfoString(lastWeek.start()); // 주차 정보 생성

        Question question = questionRepository
            .findTopQuestionBetween(lastWeek.start(), lastWeek.end(), PageRequest.of(0, 1))
            .stream().findFirst()
            .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        List<String> hashtagList = question.getAiHashtags().stream()
                                           .map(AiHashtag::getTag)
                                           .collect(Collectors.toList());

        return HotTopicResponseDTO.builder()
                                  .questionId(question.getId())
                                  .bookTitle(question.getBook().getTitle())
                                  .bookAuthor(question.getBook().getAuthor())
                                  .bookCover(question.getBook().getCoverImage())
                                  .questionTitle(question.getTitle())
                                  .aiSummary(question.getAiSummary())
                                  .hashtags(hashtagList)
                                  .weekInfo(weekInfo)
                                  .likes(question.getQuestionLikes().size())
                                  .comments(question.getAnswers().size())
                                  .views(question.getViews())
                                  .build();
    }

    @Override
    public MostQuestionedBooksResponseDTO getMostQuestionedBooks() {

        DateRange lastWeek = getLastWeekRange();
        String weekInfo = getWeekInfoString(lastWeek.start()); // 주차 정보 생성

        // 상위 7권 조회
        List<Book> books = questionRepository.findTopBooksByQuestionBetween(
            lastWeek.start(), lastWeek.end(), PageRequest.of(0, 7)
        );

        List<MostQuestionedBookResponseDTO> response = books.stream()
            .map(book -> MostQuestionedBookResponseDTO.builder()
                                                      .bookId(book.getId())
                                                      .bookTitle(book.getTitle())
                                                      .bookAuthor(book.getAuthor())
                                                      .bookCover(book.getCoverImage())
                                                      .build())
            .toList();

        return MostQuestionedBooksResponseDTO.builder()
            .weekInfo(weekInfo)
            .books(response)
            .build();
    }

    private DateRange getLastWeekRange() {

        // 조회 기준 시점: 이번 주 월요일 0시
        LocalDateTime thisMonday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();

        // 조회 기간 설정: 지난주 월요일 0시 ~ 이번 주 월요일 0시 전
        LocalDateTime startOfLastWeek = thisMonday.minusWeeks(1);

        return new DateRange(startOfLastWeek, thisMonday);
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {}

    private String getWeekInfoString(LocalDateTime date) {
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
        int month = date.getMonthValue();
        int weekOfMonth = date.get(weekFields.weekOfMonth());
        return String.format("%d월 %d주차", month, weekOfMonth);
    }

    @Override
    public WeeklyKeywordResponseDTO getWeeklyTopKeywords() {
        DateRange lastWeek = getLastWeekRange();

        log.info("[DEBUG] Querying keywords between startDate: {} and endDate: {}", lastWeek.start(), lastWeek.end());

        // 1. DB에서 키워드별 집계 데이터 조회
        List<QuestionKeywordRepository.KeywordCount> keywordCounts =
            questionKeywordRepository.findKeywordCountsBetween(lastWeek.start(), lastWeek.end());

        // 2. 대표 키워드(displayName) 기준으로 카운트 재집계
        Map<String, Long> displayNameCounts = keywordCounts.stream()
                                                           .collect(Collectors.groupingBy(
                                                               kc -> kc.getKeyword().getDisplayName(), // 대표 이름으로 그룹핑
                                                               Collectors.summingLong(QuestionKeywordRepository.KeywordCount::getCount) // 카운트 합산
                                                           ));

        // 3. 최종 결과를 DTO로 변환하고 순위 매기기
        List<WeeklyKeywordResponseDTO.KeywordInfo> finalKeywords = new ArrayList<>();
        final int[] rank = {1}; // 랭크 계산을 위한 배열

        displayNameCounts.entrySet().stream()
                         .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // 카운트 기준 내림차순 정렬
                         .limit(3) // 상위 3개만 선택
                         .forEach(entry -> {
                             finalKeywords.add(
                                 WeeklyKeywordResponseDTO.KeywordInfo.builder()
                                                                     .rank(rank[0]++)
                                                                     .keyword(entry.getKey())
                                                                     .build()
                             );
                         });

        return WeeklyKeywordResponseDTO.builder()
                                       .keywords(finalKeywords)
                                       .build();
    }
}
