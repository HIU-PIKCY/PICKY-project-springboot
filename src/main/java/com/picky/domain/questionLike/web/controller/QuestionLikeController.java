package com.picky.domain.questionLike.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.questionLike.service.QuestionLikeService;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.QuestionLikeStatusResponseDTO;
import com.picky.domain.questionLike.web.dto.QuestionLikeResponseDTO.MyLikesResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "사용자 좋아요 목록 조회 API", description = "특정 사용자가 좋아요한 모든 게시물 목록을 조회합니다.")
    @GetMapping("/members/{memberId}")
    public ApiResponse<MyLikesResponseDTO> getMyLikes(
            @Parameter(description = "조회할 사용자 ID", example = "1")
            @PathVariable Long memberId) {
        return ApiResponse.onSuccess(questionLikeService.getMyLikes(memberId));
    }
}