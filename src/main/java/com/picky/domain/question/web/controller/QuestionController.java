package com.picky.domain.question.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.question.service.QuestionService;
import com.picky.domain.question.web.dto.QuestionRequestDTO.QuestionPostRequestDTO;
import com.picky.domain.question.web.dto.QuestionResponseDTO.QuestionPostResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PostMapping("/books/{bookId}/{memberId}/questions")
    public ApiResponse<QuestionPostResponseDTO> createQuestion(@PathVariable Long bookId,
                                                               @PathVariable Long memberId,
                                                               @Valid @RequestBody QuestionPostRequestDTO request) {
        return ApiResponse.onSuccess(questionService.createQuestion(bookId, memberId, request));
    }
}
