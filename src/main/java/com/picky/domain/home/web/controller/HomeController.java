package com.picky.domain.home.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.ai.scheduler.AiSummaryScheduler;
import com.picky.domain.home.service.HomeService;
import com.picky.domain.home.web.dto.HomeResponseDTO.HotTopicResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "홈 화면")
public class HomeController {

    private final HomeService homeService;
    private final AiSummaryScheduler aiSummaryScheduler;

    @Operation(summary = "이번 주 핫 토픽 조회 API", description = "이번 주에 가장 댓글이 많이 달린 질문글 1개를 조회합니다.")
    @GetMapping("/hot-topic")
    public ApiResponse<HotTopicResponseDTO> getHotTopic() {
        return ApiResponse.onSuccess(homeService.getHotTopic());
    }

    @Profile("dev") // dev 환경에서만 활성화
    @Operation(summary = "[테스트용] 핫 토픽 AI 요약 수동 실행 API", description = "스케줄러를 기다리지 않고 AI 요약 기능을 즉시 실행합니다.")
    @PostMapping("/test/summarize")
    public ApiResponse<String> triggerSummarize() {
        aiSummaryScheduler.summarizeHotTopic();
        return ApiResponse.onSuccess("AI 요약 작업이 수동으로 실행되었습니다. 서버 로그를 확인하세요.");
    }
}
