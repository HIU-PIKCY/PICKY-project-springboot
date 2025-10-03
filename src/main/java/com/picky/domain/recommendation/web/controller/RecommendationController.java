package com.picky.domain.recommendation.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.member.entity.Member;
import com.picky.domain.recommendation.service.RecommendationService;
import com.picky.domain.recommendation.web.dto.RecommendationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@Tag(name = "책 추천")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @Operation(
            summary = "개인화 책 추천 API",
            description = "사용자가 가장 많이 사용한 키워드를 분석하여 " +
                    "같은 키워드를 사용한 다른 사용자들의 질문과 연결된 책을 추천합니다."
    )
    @GetMapping("/personalized")
    public ApiResponse<RecommendationResponseDTO> getPersonalizedRecommendation(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        return ApiResponse.onSuccess(
                recommendationService.recommendBookBasedOnKeywords(currentMember.getId())
        );
    }
}
