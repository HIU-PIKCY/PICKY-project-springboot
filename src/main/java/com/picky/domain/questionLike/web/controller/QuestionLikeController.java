package com.picky.domain.questionLike.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.questionLike.service.QuestionLikeService;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.QuestionLikeStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-like")
@Tag(name = "질문 좋아요/취소")
public class QuestionLikeController {

    private final QuestionLikeService questionLikeService;

    @Operation(summary = "질문 좋아요/취소", description = "특정 질문에 좋아요를 추가/취소합니다.")
    @PostMapping("/{questionId}/{memberId}")
    public ApiResponse<QuestionLikeStatusResponseDTO> likeQuestion(@PathVariable Long questionId, @PathVariable Long memberId) {
        return ApiResponse.onSuccess(questionLikeService.likeQuestion(questionId, memberId));
    }
}