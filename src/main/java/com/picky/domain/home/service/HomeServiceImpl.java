package com.picky.domain.home.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.home.web.dto.HomeResponseDTO.HotTopicResponseDTO;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        // 조회 기준 시점 = 이번 주 월요일 0시
        LocalDateTime thisMonday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();

        // 조회 기간 설정: 지난주 월요일 0시 ~ 이번 주 월요일 0시 전
        LocalDateTime startOfLastWeek = thisMonday.minusWeeks(1);
        LocalDateTime endOfLastWeek = thisMonday;

        Question question = questionRepository
            .findTopQuestionBetween(startOfLastWeek, endOfLastWeek, PageRequest.of(0, 1))
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
                                  .likes(question.getQuestionLikes().size())
                                  .comments(question.getAnswers().size())
                                  .views(question.getViews())
                                  .build();

    }
}
