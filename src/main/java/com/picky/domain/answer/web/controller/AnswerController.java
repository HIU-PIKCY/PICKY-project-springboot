package com.picky.domain.answer.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.answer.service.AnswerService;
import com.picky.domain.answer.web.dto.AnswerRequestDTO.AnswerCreateRequestDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerCreateResponseDTO;
import com.picky.domain.answer.web.dto.AnswerResponseDTO.AnswerListResponseDTO;
import com.picky.domain.auth.CustomerUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "답변")
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "답변 등록 API", description = "특정 질문에 대한 답변을 등록합니다.")
    @PostMapping("/questions/{questionId}/answers")
    public ApiResponse<AnswerCreateResponseDTO> createAnswer(
        @PathVariable Long questionId,
        @AuthenticationPrincipal CustomerUserDetails customerUserDetails,
        @Valid @RequestBody AnswerCreateRequestDTO request
    ) {
        Long memberId = customerUserDetails.getMember().getId();
        return ApiResponse.onSuccess(answerService.createAnswer(questionId, memberId, request));
    }

    @Operation(summary = "질문별 답변 조회 API", description = "특정 질문에 대한 모든 답변을 조회합니다.")
    @GetMapping("/questions/{questionId}/answers")
    public ApiResponse<AnswerListResponseDTO> getAnswersByQuestion(@PathVariable Long questionId, @AuthenticationPrincipal CustomerUserDetails customerUserDetails) {
        Long memberId = customerUserDetails.getMember().getId();
        return ApiResponse.onSuccess(answerService.getAnswersByQuestion(questionId, memberId));
    }

    @Operation(summary = "답변 삭제 API", description = "특정 답변을 삭제합니다.")
    @DeleteMapping("/answers/{answerId}")
    public ApiResponse<String> deleteAnswer(
        @PathVariable Long answerId,
        @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Long memberId = customerUserDetails.getMember().getId();
        answerService.deleteAnswer(answerId, memberId);
        return ApiResponse.onSuccess("답변이 성공적으로 삭제되었습니다.");
    }
}