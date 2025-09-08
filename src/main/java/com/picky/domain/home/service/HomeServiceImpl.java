package com.picky.domain.home.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.book.entity.Book;
import com.picky.domain.home.web.dto.HomeResponseDTO.HotTopicResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.MostQuestionedBookResponseDTO;
import com.picky.domain.home.web.dto.HomeResponseDTO.MostQuestionedBooksResponseDTO;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    private final QuestionRepository questionRepository;

    @Override
    public HotTopicResponseDTO getHotTopic() {

        DateRange lastWeek = getLastWeekRange();
        String weekInfo = getWeekInfoString(lastWeek.start()); // 주차 정보 생성

        Question question = questionRepository
            .findTopQuestionBetween(lastWeek.start(), lastWeek.end(), PageRequest.of(0, 1))
            .stream().findFirst()
            .orElseThrow(() -> new GeneralException(ErrorStatus.QUESTION_NOT_FOUND));

        List<String> hashtagList = List.of();
        if (question.getAiHashtags() != null && !question.getAiHashtags().isEmpty()) {
            hashtagList = Arrays.asList(question.getAiHashtags().split(","));
        }

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
}
