package com.picky.domain.ai.scheduler;

import com.picky.domain.ai.service.AiService;
import com.picky.domain.ai.service.AiService.AIResponseDTO;
import com.picky.domain.ai.service.QuestionAiUpdateService;
import com.picky.domain.answer.entity.Answer;
import com.picky.domain.question.entity.Question;
import com.picky.domain.question.repository.QuestionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSummaryScheduler {

    private final QuestionRepository questionRepository;
    private final AiService aiService;
    private final QuestionAiUpdateService questionAiUpdateService;

    @Transactional
    @Scheduled(cron = "0 42 15 * * MON") // 매주 월요일 자정에 실행
    public void summarizeHotTopic() {
        log.info("[AiSummaryScheduler] 매시간 핫토픽 요약 작업 시작");

        // 조회 종료 시점 = 이번 주 월요일 0시
        LocalDateTime endOfLastWeek = LocalDate.now().atStartOfDay();
        // 조회 시작 시점 = 종료 시점에서 일주일 전 (= 지난주 월요일 0시)
        LocalDateTime startOfLastWeek = endOfLastWeek.minusWeeks(1);

        log.info("[AiSummaryScheduler] 요약 대상 기간: {} ~ {}", startOfLastWeek, endOfLastWeek);

        Optional<Question> hotQuestionOpt = questionRepository
            .findTopQuestionBetween(startOfLastWeek, endOfLastWeek, PageRequest.of(0, 1))
            .stream().findFirst();

        if (hotQuestionOpt.isEmpty()) {
            log.info("[AiSummaryScheduler] 이번 주에 질문이 없어 요약 작업을 종료합니다.");
            return;
        }

        Question hotQuestion = hotQuestionOpt.get();

        // 댓글이 하나도 없으면 AI 요약을 시도하지 않고 바로 종료
        if (hotQuestion.getAnswers().isEmpty()) {
            log.info("[AiSummaryScheduler] 질문 ID {} 에는 댓글이 없어 요약 작업을 건너뜁니다.", hotQuestion.getId());
            return;
        }

        if (hotQuestion.getAiSummary() != null && !hotQuestion.getAiSummary().isEmpty()) {
            log.info("[AiSummaryScheduler] 질문 ID {} 는 이미 요약이 되어 있습니다. 작업을 종료합니다.", hotQuestion.getId());
            return;
        }

        log.info("[AiSummaryScheduler] 질문 ID {} 에 대해 AI 요약 작업을 시작합니다.", hotQuestion.getId());

        List<String> comments = hotQuestion.getAnswers().stream()
                                        .map(Answer::getContent)
                                        .toList();

        AIResponseDTO response = aiService.getSummaryAndHashtags(
            hotQuestion.getTitle(),
            hotQuestion.getContent(),
            comments
        ).block(); // AI 응답이 올 때까지 여기서 멈춤!

        // 응답이 성공적으로 왔을 때만 업데이트
        if (response != null) {
            log.info("[AiSummaryScheduler] OpenAI로부터 받은 실제 응답: {}", response);
            questionAiUpdateService.updateQuestionWithAiAnalysis(hotQuestion.getId(), response);
            log.info("[AiSummaryScheduler] 질문 ID {} 에 대한 AI 요약 작업이 완료 및 저장되었습니다.", hotQuestion.getId());
        } else {
            log.error("[AiSummaryScheduler] AI 서비스로부터 응답을 받지 못했습니다.");
        }
    }
}
