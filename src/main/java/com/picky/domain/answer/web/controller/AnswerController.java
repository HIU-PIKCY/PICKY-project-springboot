package com.picky.domain.answer.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.answer.service.AnswerService;
import com.picky.domain.answer.web.dto.AnswerRequestDTO.AnswerCreateRequestDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerCreateResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.MyAnswersResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "답변")
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "사용자 답변 목록 조회 API", description = "특정 사용자가 작성한 모든 답변 목록을 조회합니다.")
    @GetMapping("/members/{memberId}/answers")
    public ApiResponse<MyAnswersResponseDTO> getMyAnswers(
        @Parameter(description = "조회할 사용자 ID", example = "1")
        @PathVariable Long memberId) {
        return ApiResponse.onSuccess(answerService.getMyAnswers(memberId));
    }

    @Operation(summary = "답변 등록 API", description = "특정 질문에 대한 답변을 등록합니다.")
    @PostMapping("/questions/{questionId}/answers/{memberId}")
    public ApiResponse<AnswerCreateResponseDTO> createAnswer(
        @PathVariable Long questionId,
        @PathVariable Long memberId,
        @Valid @RequestBody AnswerCreateRequestDTO request
    ) {
        return ApiResponse.onSuccess(answerService.createAnswer(questionId, memberId, request));
    }
}