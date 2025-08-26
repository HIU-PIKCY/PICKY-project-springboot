package com.picky.domain.question.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.question.service.QuestionService;
import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionDetailResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionListResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.MyQuestionsResponseDTO;
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
@Tag(name = "질문")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "질문 등록 API", description = "사람이 질문을 등록합니다.")
    @PostMapping("/books/{bookId}/questions/{memberId}")
    public ApiResponse<QuestionPostResponseDTO> createQuestion(@PathVariable Long bookId,
                                                               @PathVariable Long memberId,
                                                               @Valid @RequestBody QuestionPostRequestDTO request) {
        return ApiResponse.onSuccess(questionService.createQuestion(bookId, memberId, request));
    }

    @Operation(summary = "질문 상세 조회 API", description = "질문 상세 정보를 조회합니다.")
    @GetMapping("/questions/{questionId}/{memberId}")
    public ApiResponse<QuestionDetailResponseDTO> getQuestionDetail(@PathVariable Long questionId,
                                                                    @PathVariable Long memberId) {
        return ApiResponse.onSuccess(questionService.getQuestionDetail(questionId, memberId));
    }

    @Operation(summary = "책에 대한 질문 목록 조회 API", description = "책에 대한 질문 목록을 조회합니다.")
    @GetMapping("/books/{bookId}/questions")
    public ApiResponse<QuestionListResponseDTO> getQuestionList(@PathVariable Long bookId) {
        return ApiResponse.onSuccess(questionService.getQuestionList(bookId));
    }

    @Operation(summary = "사용자 질문 목록 조회 API", description = "특정 사용자가 작성한 모든 질문 목록을 조회합니다.")
    @GetMapping("/members/{memberId}/questions")
    public ApiResponse<MyQuestionsResponseDTO> getMyQuestions(
            @Parameter(description = "조회할 사용자 ID", example = "1")
            @PathVariable Long memberId) {
        return ApiResponse.onSuccess(questionService.getMyQuestions(memberId));
    }
}
